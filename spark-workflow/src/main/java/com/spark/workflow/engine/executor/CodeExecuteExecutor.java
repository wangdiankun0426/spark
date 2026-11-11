package com.spark.workflow.engine.executor;

import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.manage.BaseService;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 18:00:00
 * 代码执行节点执行器：以子进程运行用户Python脚本，stdin传参、stdout收结果
 * 执行边界：脚本以应用进程权限在本机运行，生产部署建议容器隔离或低权限用户运行python
 */
@Component
public class CodeExecuteExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CodeExecuteExecutor.class);

    /**
     * 结果分隔标记：runner 在 stdout 上先打标记再写 JSON，兼容用户脚本内 print 调试输出
     */
    private static final String RESULT_MARK = "\n###SPARK_RESULT###";

    /**
     * 用户脚本大小上限
     */
    private static final int MAX_CODE_LENGTH = 64 * 1024;

    /**
     * 脚本stdout上限
     */
    private static final int MAX_OUTPUT_LENGTH = 1024 * 1024;

    /**
     * 脚本stderr保留上限
     */
    private static final int MAX_ERROR_LENGTH = 4096;

    /**
     * 流读取线程回收等待毫秒数
     */
    private static final long STREAM_JOIN_MS = 5000L;

    /**
     * 默认执行超时毫秒数，节点执行策略未配置时使用
     */
    private static final long DEFAULT_TIMEOUT_MS = 30000L;

    /**
     * 输入参数名合法性：字母下划线开头，仅字母数字下划线，保证Python侧可直接按键取用
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * 执行壳：与用户代码分离，统一入参解析、结果序列化与异常出口
     */
    private static final String RUNNER_TEMPLATE = """
            import json
            import os
            import sys

            # 隔离模式不加载脚本目录，显式加入以导入main
            sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
            try:
                params = json.loads(sys.stdin.read() or "{}")
                from main import main
                result = main(params)
                if not isinstance(result, dict):
                    result = {"result": result}
                sys.stdout.write("\\n###SPARK_RESULT###")
                sys.stdout.write(json.dumps(result, ensure_ascii=False, default=str))
            except Exception as e:
                sys.stderr.write("%s: %s" % (e.__class__.__name__, e))
                sys.exit(1)
            """;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.CODE_EXECUTE.getValue();
    }

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 节点输出
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        String code = MapUtil.getStringVal(config, "code");
        if (StringUtil.isBlank(code)) {
            throw new IllegalArgumentException("代码执行节点未配置代码");
        }
        if (code.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("代码执行节点脚本过大，请勿超过64KB");
        }
        List<Map<String, Object>> inputs = JsonUtil.getMapList(config, "inputs");
        Map<String, String> params = this.buildParams(input, inputs, valueMap, showValueMap);
        String paramsJson = JsonUtil.toString(params);
        long timeoutMs = this.resolveTimeout(config);
        String resultJson = this.runPython(code, paramsJson, timeoutMs);
        return this.buildOutput(nodeId, resultJson);
    }

    /**
     * 组装脚本入参：上游连线输出原键并入，再叠加显式输入参数映射
     * @param input 当前节点输入
     * @param inputs 显式输入参数配置
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 参数map
     */
    private Map<String, String> buildParams(Map<String, String> input, List<Map<String, Object>> inputs, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, String> params = new LinkedHashMap<>();
        if (input != null) {
            params.putAll(input);
        }
        if (inputs == null) {
            return params;
        }
        for (Map<String, Object> item : inputs) {
            String name = MapUtil.getStringVal(item, "name");
            if (StringUtil.isBlank(name) || !NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException("代码执行节点输入参数名非法：" + name);
            }
            String valueCode = MapUtil.getStringVal(item, "value");
            String value = super.generateFlowValue(valueCode, null, valueMap, showValueMap, input);
            params.put(name, value == null ? "" : value);
        }
        return params;
    }

    /**
     * 解析超时：节点执行策略优先，未配置用默认值
     * @param config 节点配置
     * @return 超时毫秒数
     */
    private long resolveTimeout(Map<String, Object> config) {
        Long nodeTimeoutMs = MapUtil.getLongVal(config, "timeoutMs", null);
        if (nodeTimeoutMs != null && nodeTimeoutMs > 0) {
            return nodeTimeoutMs;
        }
        return DEFAULT_TIMEOUT_MS;
    }

    /**
     * 运行Python子进程：临时目录写脚本与壳，超时与非零退出码强杀进程
     * @param code 用户脚本
     * @param paramsJson 入参JSON
     * @param timeoutMs 超时毫秒数
     * @return 结果JSON串
     */
    private String runPython(String code, String paramsJson, long timeoutMs) {
        Path workDir = null;
        Process process = null;
        try {
            workDir = Files.createTempDirectory("wf-py-");
            Path mainFile = workDir.resolve("main.py");
            Files.writeString(mainFile, code);
            Path runnerFile = workDir.resolve("runner.py");
            Files.writeString(runnerFile, RUNNER_TEMPLATE);
            ProcessBuilder builder = new ProcessBuilder("python3", "-I", "-B", "runner.py");
            builder.directory(workDir.toFile());
            process = builder.start();
            // 两条输出流必须并发消费，管道写满会导致子进程阻塞假死
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            final InputStream outStream = process.getInputStream();
            final InputStream errStream = process.getErrorStream();
            Thread outThread = new Thread(() -> this.readStream(outStream, stdout, MAX_OUTPUT_LENGTH));
            Thread errThread = new Thread(() -> this.readStream(errStream, stderr, MAX_ERROR_LENGTH));
            outThread.start();
            errThread.start();
            this.writeStdin(process, paramsJson);
            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("代码执行超时，超过" + timeoutMs + "ms");
            }
            outThread.join(STREAM_JOIN_MS);
            errThread.join(STREAM_JOIN_MS);
            if (stdout.length() >= MAX_OUTPUT_LENGTH) {
                throw new RuntimeException("代码执行输出过大，请勿超过1MB");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("代码执行失败：" + stderr);
            }
            return this.extractResult(stdout.toString());
        } catch (IOException e) {
            logger.error("codeExecute python launch failed, errMsg={}", e.getMessage(), e);
            throw new RuntimeException("Python解释器不可用，请检查部署环境是否安装python3", e);
        } catch (InterruptedException e) {
            if (process != null) {
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            throw new RuntimeException("代码执行被中断", e);
        } finally {
            this.cleanWorkDir(workDir);
        }
    }

    /**
     * 写入stdin参数后关闭，runner以EOF触发读取
     * @param process 子进程
     * @param paramsJson 入参JSON
     * @throws IOException 写入失败
     */
    private void writeStdin(Process process, String paramsJson) throws IOException {
        try (OutputStream os = process.getOutputStream()) {
            os.write(paramsJson.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    /**
     * 读取进程输出流，超出上限只排空不累积
     * @param stream 输出流
     * @param target 累积容器
     * @param maxLength 累积上限
     */
    private void readStream(InputStream stream, StringBuilder target, int maxLength) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (target.length() >= maxLength) {
                    continue;
                }
                target.append(line).append('\n');
            }
        } catch (IOException e) {
            logger.warn("codeExecute stream read failed, errMsg={}", e.getMessage());
        }
    }

    /**
     * 从stdout截取结果JSON，丢弃结果标记前的调试print输出
     * @param stdoutText 进程stdout全文
     * @return 结果JSON串
     */
    private String extractResult(String stdoutText) {
        int index = stdoutText.lastIndexOf(RESULT_MARK);
        if (index < 0) {
            throw new RuntimeException("脚本未返回结果，请检查main函数是否return");
        }
        String jsonText = stdoutText.substring(index + RESULT_MARK.length()).trim();
        if (StringUtil.isBlank(jsonText) || !JsonUtil.isValidJson(jsonText)) {
            throw new RuntimeException("脚本返回结果非法，main函数需return字典");
        }
        return jsonText;
    }

    /**
     * 结果字典转节点输出：键带nodeId前缀直拼，复合结构序列化为字符串
     * @param nodeId 节点id
     * @param resultJson 结果JSON串
     * @return 节点输出
     */
    private Map<String, String> buildOutput(String nodeId, String resultJson) {
        Map<String, Object> resultMap = JsonUtil.toMap(resultJson);
        if (resultMap == null) {
            throw new RuntimeException("脚本返回结果非法，main函数需return字典");
        }
        Map<String, String> output = new HashMap<>();
        output.put(nodeId + "result", resultJson);
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            Object value = entry.getValue();
            String text;
            if (value == null) {
                text = "";
            } else if (value instanceof Map || value instanceof List) {
                text = JsonUtil.toString(value);
            } else {
                text = String.valueOf(value);
            }
            output.put(nodeId + entry.getKey(), text);
        }
        logger.info("codeExecute python finished, nodeId={}, outputCount={}", nodeId, resultMap.size());
        return output;
    }

    /**
     * 删除本次执行的临时目录
     * @param workDir 工作目录
     */
    private void cleanWorkDir(Path workDir) {
        if (workDir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(workDir)) {
            List<Path> files = paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path path : files) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            logger.warn("codeExecute work dir cleanup failed, dir={}", workDir);
        }
    }
}

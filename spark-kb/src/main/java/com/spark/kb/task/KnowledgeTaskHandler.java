package com.spark.kb.task;

import com.spark.bean.base.ResultData;
import com.spark.bean.task.result.TaskInstanceResult;
import com.spark.constant.TaskParamCode;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.TaskTypeEnum;
import com.spark.kb.service.IDocumentService;
import com.spark.task.service.ITaskTypeHandler;
import com.spark.utils.CollectionUtil;
import com.spark.utils.JsonUtil;
import com.spark.utils.MapUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/21 10:16
 */
@Service
public class KnowledgeTaskHandler implements ITaskTypeHandler {
    private final static Logger logger = LoggerFactory.getLogger(KnowledgeTaskHandler.class);
    @Autowired
    private IDocumentService documentService;

    /**
     * 处理的任务类型
     * @return
     */
    @Override
    public Integer getTaskType() {
        return TaskTypeEnum.KB_FILE_ARCHIVE.getValue();
    }

    /**
     * 执行任务
     * @param taskInstance 任务实例
     * @param params 任务参数
     * @return 任务执行结果，成功时data为输出JSON字符串
     */
    @Override
    public ResultData<Map<String, String>> handle(TaskInstanceResult taskInstance, Map<String, String> params) {
        ResultData<Map<String, String>> result = new ResultData<>();
        logger.info("KnowledgeTaskHandler taskInstance={},params={}", taskInstance, params);
        String kbIdStr = MapUtil.getStringVal(params, TaskParamCode.KNOWLEDGE_ID);
        if (StringUtil.isBlank(kbIdStr)) {
            logger.error("kbId param not exist, taskId={}", taskInstance.getId());
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String attIdStr = MapUtil.getStringVal(params, TaskParamCode.ATT_ID);
        if (StringUtil.isBlank(attIdStr)) {
            logger.error("attId param not exist, taskId={}", taskInstance.getId());
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        List<Long> kbIds = Arrays.stream(kbIdStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        List<Long> attIds = Arrays.stream(attIdStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        if (CollectionUtil.isEmpty(kbIds) || CollectionUtil.isEmpty(attIds)) {
            logger.error("kbIds or attIds param not exist, taskId={}", taskInstance.getId());
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Map<String, String> outputMap = new HashMap<>();
        for (Long kbId : kbIds) {
            for (Long attId : attIds) {
                ResultData<Long> fileResult = documentService.fileDocument(attId, kbId);
                if (fileResult.getCode() != ResultData.OK) {
                    logger.error("file document fail, attId={}, kbId={}, taskId={}", attId, kbId, taskInstance.getId());
                    return result;
                }
                Long fileId = fileResult.getData();
                if (fileId != null) {
                    outputMap.put(TaskParamCode.FILE_ID + attId, fileId.toString());
                }
            }
        }
        logger.info("task success, taskId={}, instanceId={}", taskInstance.getId(), taskInstance.getObjId());
        result.setData(outputMap);
        result.setCode(ResultData.OK);
        return result;
    }
}

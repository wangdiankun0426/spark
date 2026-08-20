package com.spark.kb.task;

import com.spark.bean.base.ResultData;
import com.spark.bean.task.result.TaskInstanceResult;
import com.spark.constant.TaskParamCode;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.TaskTypeEnum;
import com.spark.kb.service.IDocumentService;
import com.spark.task.service.ITaskTypeHandler;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
     * @param taskInstance
     * @param params
     * @return
     */
    @Override
    public ResultData<Void> handle(TaskInstanceResult taskInstance, Map<String, String> params) {
        ResultData<Void> result = new ResultData<>();
        logger.info("taskInstance={},params={}", taskInstance, params);
        String kbIdStr = params.getOrDefault(TaskParamCode.KNOWLEDGE_ID, null);
        if (StringUtil.isBlank(kbIdStr)) {
            logger.error("kbId param not exist, taskId={}", taskInstance.getId());
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String attIdStr = params.getOrDefault(TaskParamCode.ATT_ID, null);
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
        for (Long kbId : kbIds) {
            for (Long attId : attIds) {
                ResultData<Long> fileResult = documentService.fileDocument(attId, kbId);
                if (fileResult.getCode() != ResultData.OK) {
                    logger.error("file document fail, attId={}, kbId={}, taskId={}", attId, kbId, taskInstance.getId());
                    return result;
                }
            }
        }
        logger.info("task success, taskId={}, instanceId={}", taskInstance.getId(), taskInstance.getObjId());
        result.setCode(ResultData.OK);
        return result;
    }
}

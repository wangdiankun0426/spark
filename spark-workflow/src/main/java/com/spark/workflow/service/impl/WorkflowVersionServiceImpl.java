package com.spark.workflow.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.workflow.entity.WfTemplate;
import com.spark.common.bean.workflow.entity.WfTemplateVersion;
import com.spark.common.bean.workflow.query.WfTemplateQuery;
import com.spark.common.bean.workflow.query.WfTemplateVersionQuery;
import com.spark.common.bean.workflow.result.WfTemplateResult;
import com.spark.common.bean.workflow.result.WfTemplateVersionResult;
import com.spark.common.bean.workflow.vo.WfTemplateVersionVO;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.workflow.WfTemplateDao;
import com.spark.dao.workflow.WfTemplateVersionDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.workflow.service.IWorkflowVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 */
@Service
@LogPrint
public class WorkflowVersionServiceImpl extends BaseService<WfTemplateVersionQuery, WfTemplateVersionResult> implements IWorkflowVersionService {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowVersionServiceImpl.class);
    @Autowired
    private WfTemplateVersionDao templateVersionDao;
    @Autowired
    private WfTemplateDao templateDao;

    /**
     * 保存版本
     *
     * @param versionVO 版本参数
     * @return 保存结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.WORKFLOW_VERSION_SAVE)
    public ResultData<Void> saveVersion(WfTemplateVersionVO versionVO) {
        ResultData<Void> result = new ResultData<>();
        if (versionVO == null || versionVO.getTemplateId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateQuery templateQuery = new WfTemplateQuery();
        templateQuery.setId(versionVO.getTemplateId());
        WfTemplateResult templateResult = templateDao.queryTemplate(templateQuery);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_NOT_FOUND);
            return result;
        }
        Integer maxRevCode = templateVersionDao.queryMaxRevCode(versionVO.getTemplateId());
        int newRevCode = (maxRevCode == null ? 0 : maxRevCode) + 1;
        String newRevNum = String.format("%.1f", newRevCode / 10.0);
        WfTemplateVersion version = new WfTemplateVersion();
        version.setTemplateId(versionVO.getTemplateId());
        version.setRevCode(newRevCode);
        version.setRevNum(newRevNum);
        version.setDagJson(versionVO.getDagJson());
        version.setGlobalVars(versionVO.getGlobalVars());
        int count = templateVersionDao.insertDB(version);
        if (count < 1) {
            logger.error("saveVersion error, insert version db fail");
            return result;
        }
        WfTemplate template = new WfTemplate();
        template.setId(versionVO.getTemplateId());
        template.setRevId(version.getId());
        template.setRevNum(newRevNum);
        template.setFormId(templateResult.getFormId());
        count = templateDao.updateDBById(template);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询版本详情
     * @param query
     * @return
     */
    @Override
    public ResultData<WfTemplateVersionResult> queryVersionDetail(WfTemplateVersionQuery query) {
        ResultData<WfTemplateVersionResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateVersionResult versionResult = templateVersionDao.queryVersion(query);
        if (versionResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_VERSION_NOT_FOUND);
            return result;
        }
        result.setData(versionResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<WfTemplateVersionResult> list) {
        if (CollectionUtil.isEmpty(list)) { return; }
        super.supplyCreatedByName(list);
        // 列表不返回完整dag_json
        list.forEach(item -> item.setDagJson(null));
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return
     */
    @Override
    protected int queryCount(WfTemplateVersionQuery query) { return templateVersionDao.queryVersionCount(query); }

    /**
     * 查询列表
     * @param query 查询参数
     * @return
     */
    @Override
    protected List<WfTemplateVersionResult> queryList(WfTemplateVersionQuery query) { return templateVersionDao.queryVersionList(query); }
}

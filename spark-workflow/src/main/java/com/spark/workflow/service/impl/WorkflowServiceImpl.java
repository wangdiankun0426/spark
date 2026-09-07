package com.spark.workflow.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.workflow.entity.WfTemplate;
import com.spark.common.bean.workflow.entity.WfTemplateVersion;
import com.spark.common.bean.workflow.query.WfTemplateQuery;
import com.spark.common.bean.workflow.query.WfInstanceQuery;
import com.spark.common.bean.workflow.result.WfTemplateResult;
import com.spark.common.bean.workflow.vo.WfTemplateVO;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.query.FormVersionQuery;
import com.spark.common.bean.form.result.FormResult;
import com.spark.common.bean.form.result.FormVersionResult;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.workflow.WfTemplateDao;
import com.spark.dao.workflow.WfTemplateVersionDao;
import com.spark.dao.workflow.WfInstanceDao;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.manage.BaseService;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.common.utils.TextUtil;
import com.spark.workflow.service.IWorkflowService;
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
public class WorkflowServiceImpl extends BaseService<WfTemplateQuery, WfTemplateResult> implements IWorkflowService {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);
    @Autowired
    private WfTemplateDao templateDao;
    @Autowired
    private WfTemplateVersionDao templateVersionDao;
    @Autowired
    private WfInstanceDao instanceDao;
    @Autowired
    private FormDao formDao;
    @Autowired
    private FormVersionDao formVersionDao;

    /**
     * 创建工作流
     * @param templateVO
     * @return
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.WORKFLOW_INSERT)
    public ResultData<Void> createWorkflow(WfTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || StringUtil.isBlank(templateVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 输入表单绑定可选，绑定时校验表单存在
        if (templateVO.getFormId() != null && !checkFormExist(templateVO.getFormId())) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Long templateId = super.genObjectId(ObjectTypeEnum.WORKFLOW);
        WfTemplateVersion version = new WfTemplateVersion();
        version.setTemplateId(templateId);
        version.setRevCode(1);
        version.setRevNum("0.1");
        int count = templateVersionDao.insertDB(version);
        if (count < 1) {
            logger.error("createWorkflow error, insert version db fail");
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        WfTemplate template = new WfTemplate();
        BeanUtil.copyProperties(templateVO, template);
        template.setId(templateId);
        template.setStatus(templateVO.getStatus() == null ? StatusEnum.ABNORMAL.getValue() : templateVO.getStatus());
        template.setRevId(version.getId());
        template.setRevNum("0.1");
        count = templateDao.insertDB(template);
        if (count < 1) {
            logger.error("createWorkflow error, insert template db fail");
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(templateId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改工作流
     * @param templateVO
     * @return
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.WORKFLOW_UPDATE)
    public ResultData<Void> updateWorkflow(WfTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || templateVO.getId() == null || templateVO.getFormId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateQuery query = new WfTemplateQuery();
        query.setId(templateVO.getId());
        WfTemplateResult templateResult = templateDao.queryTemplate(query);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_NOT_FOUND);
            return result;
        }
        // 输入表单绑定可选，绑定时校验表单存在
        if (templateVO.getFormId() != null && !checkFormExist(templateVO.getFormId())) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        WfTemplate template = new WfTemplate();
        BeanUtil.copyProperties(templateVO, template);
        int count = templateDao.updateDBById(template);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(templateVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除工作流
     * @param templateVO
     * @return
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.WORKFLOW_DELETE)
    public ResultData<Void> deleteWorkflow(WfTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || templateVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateQuery query = new WfTemplateQuery();
        query.setId(templateVO.getId());
        WfTemplateResult templateResult = templateDao.queryTemplate(query);
        if (templateResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        WfInstanceQuery runQuery = new WfInstanceQuery();
        runQuery.setTemplateId(templateVO.getId());
        int runCount = instanceDao.queryInstanceCount(runQuery);
        if (runCount > 0) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_HAS_RUN_HISTORY);
            return result;
        }
        WfTemplate template = new WfTemplate();
        template.setId(templateVO.getId());
        int count = templateDao.deleteDBById(template);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(templateVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询工作流
     * @param query
     * @return
     */
    @Override
    @DataScope
    public ResultData<PageResult<WfTemplateResult>> pageWorkflowList(WfTemplateQuery query) {
        ResultData<PageResult<WfTemplateResult>> result = new ResultData<>();
        if (query == null) { query = new WfTemplateQuery(); }
        result.setData(super.pageList(query));
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询工作流详情
     * @param query
     * @return
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.WORKFLOW_DETAIL)
    public ResultData<WfTemplateResult> queryWorkflowDetail(WfTemplateQuery query) {
        ResultData<WfTemplateResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateResult templateResult = templateDao.queryTemplate(query);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_NOT_FOUND);
            return result;
        }
        fillFormJson(templateResult);
        result.setData(templateResult);
        result.setObjId(templateResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验表单是否存在
     *
     * @param formId 表单ID
     * @return 是否存在
     */
    private boolean checkFormExist(Long formId) {
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        return formDao.queryForm(formQuery) != null;
    }

    /**
     * 填充绑定表单JSON定义
     *
     * @param templateResult 模板结果
     */
    private void fillFormJson(WfTemplateResult templateResult) {
        if (templateResult.getFormId() == null) {
            return;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(templateResult.getFormId());
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            return;
        }
        templateResult.setFormRevId(formResult.getRevId());
        if (formResult.getRevId() == null) {
            return;
        }
        FormVersionQuery formVersionQuery = new FormVersionQuery();
        formVersionQuery.setId(formResult.getRevId());
        FormVersionResult formVersionResult = formVersionDao.queryFormVersion(formVersionQuery);
        if (formVersionResult == null || StringUtil.isBlank(formVersionResult.getFilePath())) {
            return;
        }
        ResultData<String> fromText = TextUtil.getFromText(formVersionResult.getFilePath(), false);
        templateResult.setFormJson(fromText.getData());
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<WfTemplateResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        list.forEach(item -> item.setStatusName(StatusEnum.indexOf(item.getStatus()).getDesc()));
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return
     */
    @Override
    protected int queryCount(WfTemplateQuery query) { return templateDao.queryTemplateCount(query); }

    /**
     * 拆线呢列表
     * @param query 查询参数
     * @return
     */
    @Override
    protected List<WfTemplateResult> queryList(WfTemplateQuery query) { return templateDao.queryTemplateList(query); }

    /**
     * 查询最大id
     * @return
     */
    @Override
    protected Long queryMaxId() { return templateDao.queryTemplateMaxId(); }
}

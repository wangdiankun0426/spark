package com.spark.workflow.service.impl;

import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
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
import com.spark.common.enums.DeleteFlagEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
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
import org.springframework.transaction.annotation.Transactional;

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
    @LogOperate(operateType = OperateTypeEnum.WORKFLOW_INSERT)
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
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
            throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
        }
        WfTemplate template = new WfTemplate();
        BeanUtil.copyProperties(templateVO, template);
        // 非站点管理员强制归属当前租户
        if (!SessionHolder.isSysAdmin()) {
            template.setTenantId(SessionHolder.getCurrentTenantId());
        }
        template.setId(templateId);
        template.setStatus(templateVO.getStatus() == null ? StatusEnum.ABNORMAL.getValue() : templateVO.getStatus());
        template.setRevId(version.getId());
        template.setRevNum("0.1");
        count = templateDao.insertDB(template);
        if (count < 1) {
            logger.error("createWorkflow error, insert template db fail");
            throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
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
    @LogOperate(operateType = OperateTypeEnum.WORKFLOW_UPDATE)
    public ResultData<Void> updateWorkflow(WfTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || templateVO.getId() == null || templateVO.getFormId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateQuery query = new WfTemplateQuery();
        query.setId(templateVO.getId());
        super.scopeTenant(query);
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
    @LogOperate(operateType = OperateTypeEnum.WORKFLOW_DELETE)
    public ResultData<Void> deleteWorkflow(WfTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || templateVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateQuery query = new WfTemplateQuery();
        query.setId(templateVO.getId());
        super.scopeTenant(query);
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
        if (query == null) {
            query = new WfTemplateQuery();
        }
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
    @LogOperate(operateType = OperateTypeEnum.WORKFLOW_DETAIL)
    public ResultData<WfTemplateResult> queryWorkflowDetail(WfTemplateQuery query) {
        ResultData<WfTemplateResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        WfTemplateResult templateResult = templateDao.queryTemplate(query);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_NOT_FOUND);
            return result;
        }
        ResultData<Void> fillResult = this.fillFormJson(templateResult);
        if (fillResult.getCode() != ResultData.OK) {
            int errorCode = fillResult.getCode();
            String errorMessage = fillResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        result.setData(templateResult);
        result.setObjId(templateResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 统计可用工作流数量，与工作流列表同口径
     * @param query 查询参数
     * @return 数量结果
     */
    @Override
    @DataScope
    public ResultData<Integer> countMyWorkflows(WfTemplateQuery query) {
        ResultData<Integer> result = new ResultData<>();
        if (query == null) {
            query = new WfTemplateQuery();
        }
        if (!SessionHolder.isSysAdmin()) {
            query.setTenantId(SessionHolder.getCurrentTenantId());
        }
        int count = templateDao.queryTemplateCount(query);
        result.setData(count);
        result.setCode(ResultData.OK);
        return result;
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

    /**
     * 校验表单是否存在
     * @param formId 表单id
     * @return true表单存在且存活
     */
    private boolean checkFormExist(Long formId) {
        if (formId == null) {
            return false;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        super.scopeTenant(formQuery);
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            return false;
        }
        Integer deleteFlag = formResult.getDeleteFlag();
        Integer normalFlag = DeleteFlagEnum.NORMAL.getValue();
        return normalFlag.equals(deleteFlag);
    }

    /**
     * 填充绑定表单JSON定义
     * @param templateResult 模板结果
     * @return 填充结果
     */
    private ResultData<Void> fillFormJson(WfTemplateResult templateResult) {
        ResultData<Void> result = new ResultData<>();
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long formId = templateResult.getFormId();
        if (formId == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        super.scopeTenant(formQuery);
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Integer deleteFlag = formResult.getDeleteFlag();
        Integer normalFlag = DeleteFlagEnum.NORMAL.getValue();
        if (!normalFlag.equals(deleteFlag)) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Long formRevId = formResult.getRevId();
        templateResult.setFormRevId(formRevId);
        if (formRevId == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            return result;
        }
        FormVersionQuery formVersionQuery = new FormVersionQuery();
        formVersionQuery.setId(formRevId);
        formVersionQuery.setFormId(formId);
        FormVersionResult formVersionResult = formVersionDao.queryFormVersion(formVersionQuery);
        if (formVersionResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            return result;
        }
        String filePath = formVersionResult.getFilePath();
        if (StringUtil.isBlank(filePath)) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            result.setMessage("表单版本内容不存在");
            return result;
        }
        ResultData<String> textResult = TextUtil.getFromText(filePath, false);
        if (textResult.getCode() != ResultData.OK) {
            int errorCode = textResult.getCode();
            String errorMessage = textResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        String formJson = textResult.getData();
        templateResult.setFormJson(formJson);
        result.setCode(ResultData.OK);
        return result;
    }
}

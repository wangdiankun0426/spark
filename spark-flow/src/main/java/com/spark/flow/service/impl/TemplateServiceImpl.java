package com.spark.flow.service.impl;

import com.spark.config.aspectj.annotation.DataScope;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.flow.entity.FlowTemplate;
import com.spark.bean.flow.entity.FlowTemplateVersion;
import com.spark.bean.flow.query.FlowTemplateQuery;
import com.spark.bean.flow.query.FlowTemplateVersionQuery;
import com.spark.bean.flow.result.FlowTemplateResult;
import com.spark.bean.flow.result.FlowTemplateVersionResult;
import com.spark.bean.flow.vo.FlowTemplateVO;
import com.spark.bean.form.query.FormQuery;
import com.spark.bean.form.query.FormVersionQuery;
import com.spark.bean.form.result.FormResult;
import com.spark.bean.form.result.FormVersionResult;
import com.spark.dao.flow.FlowTemplateDao;
import com.spark.dao.flow.FlowTemplateVersionDao;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.FlowTypeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.flow.service.IFlowTemplateService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import com.spark.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.utils.BeanUtil;
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
 * @since 2025-10-29 21:15:35
 */
@Service
public class TemplateServiceImpl extends BaseService<FlowTemplateQuery, FlowTemplateResult> implements IFlowTemplateService {
    private final static Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);
    @Autowired
    private FlowTemplateDao templateDao;
    @Autowired
    private FlowTemplateVersionDao templateVersionDao;
    @Autowired
    private FormDao formDao;
    @Autowired
    private FormVersionDao formVersionDao;

    /**
     * 创建流程模板
     * @param templateVO 流程模板数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createTemplate(FlowTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || StringUtil.isBlank(templateVO.getName()) || templateVO.getFormId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long templateId = super.genObjectId(ObjectTypeEnum.FLOW_TEMPLATE);
        String processId = "process_"+ System.currentTimeMillis();
        FlowTemplateVersion templateVersion = new FlowTemplateVersion();
        templateVersion.setTemplateId(templateId);
        templateVersion.setRevNum("0.1");
        templateVersion.setRevCode(1);
        templateVersion.setProcessId(processId);
        int count = templateVersionDao.insertDB(templateVersion);
        if (count < 1) {
            logger.error("createTemplate error, insert db fail");
            return result;
        }
        FlowTemplate template = new FlowTemplate();
        BeanUtil.copyProperties(templateVO, template);
        template.setProcessId(processId);
        template.setStatus(StatusEnum.ABNORMAL.getValue());
        template.setType(templateVO.getType() == null ? FlowTypeEnum.NORMAL.getValue() : templateVO.getType());
        template.setRevNum("0.1");
        template.setRevId(templateVersion.getId());
        template.setId(templateId);
        count = templateDao.insertDB(template);
        if (count < 1) {
            logger.error("createTemplate error, insert db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改流程模板
     * @param templateVO 流程模板数据
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateTemplate(FlowTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || templateVO.getId() == null || templateVO.getFormId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowTemplateQuery templateQuery = new FlowTemplateQuery();
        templateQuery.setId(templateVO.getId());
        FlowTemplateResult templateResult = templateDao.queryTemplate(templateQuery);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
            return result;
        }
        FlowTemplate template = new FlowTemplate();
        BeanUtil.copyProperties(templateVO, template);
        int count = templateDao.updateDBById(template);
        if (count < 1) {
            logger.error("updateTemplate error, update db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除流程模板
     * @param templateVO 删除流程模板数据
     * @return 删除结果
     */
    @Override
    public ResultData<Void> deleteTemplate(FlowTemplateVO templateVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVO == null || templateVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowTemplateQuery templateQuery = new FlowTemplateQuery();
        templateQuery.setId(templateVO.getId());
        FlowTemplateResult templateResult = templateDao.queryTemplate(templateQuery);
        if (templateResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        FlowTemplate template = new FlowTemplate();
        template.setId(templateVO.getId());
        int count = templateDao.deleteDBById(template);
        if (count < 1) {
            logger.error("deleteTemplate error, delete db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询流程模板
     * @param query 查询参数
     * @return 查询结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<FlowTemplateResult>> pageTemplateList(FlowTemplateQuery query) {
        ResultData<PageResult<FlowTemplateResult>> result = new ResultData<>();
        if (query == null) {
            query = new FlowTemplateQuery();
        }
        PageResult<FlowTemplateResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询流程模板详情
     * @param query 查询参数
     * @return 查询结果
     */
    @Override
    public ResultData<FlowTemplateResult> queryTemplateDetail(FlowTemplateQuery query) {
        ResultData<FlowTemplateResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowTemplateResult templateResult = templateDao.queryTemplate(query);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
            return result;
        }
        result.setData(templateResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 显示流程模板详情
     * @param query 显示流程模板详情参数
     * @return 显示结果
     */
    @Override
    public ResultData<FlowTemplateResult> showTemplateDetail(FlowTemplateQuery query) {
        ResultData<FlowTemplateResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowTemplateResult templateResult = templateDao.queryTemplate(query);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
            return result;
        }
        Long formId = templateResult.getFormId();
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        FormVersionQuery formVersionQuery = new FormVersionQuery();
        formVersionQuery.setId(formResult.getRevId());
        FormVersionResult formVersionResult = formVersionDao.queryFormVersion(formVersionQuery);
        if (formVersionResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            return result;
        }
        templateResult.setFormRevId(formVersionResult.getId());
        String formPath = formVersionResult.getFilePath();
        if (StringUtil.isNotBlank(formPath)) {
            ResultData<String> fromText = TextUtil.getFromText(formPath, false);
            templateResult.setFormJson(fromText.getData());
        }
        FlowTemplateVersionQuery templateVersionQuery = new FlowTemplateVersionQuery();
        templateVersionQuery.setId(templateResult.getRevId());
        FlowTemplateVersionResult templateVersionResult = templateVersionDao.queryTemplateVersion(templateVersionQuery);
        if (templateVersionResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        String bpmPath = templateVersionResult.getBpmPath();
        if (StringUtil.isNotBlank(bpmPath)) {
            ResultData<String> fromText = TextUtil.getFromText(bpmPath, false);
            templateResult.setBpmJson(fromText.getData());
        }
        result.setData(templateResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<FlowTemplateResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        list.forEach(item -> {
            item.setStatusName(StatusEnum.indexOf(item.getStatus()).getDesc());
            item.setTypeName(FlowTypeEnum.indexOf(item.getType()).getDesc());
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(FlowTemplateQuery query) {
        return templateDao.queryTemplateCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<FlowTemplateResult> queryList(FlowTemplateQuery query) {
        return templateDao.queryTemplateList(query);
    }

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() {
        return templateDao.queryTemplateMaxId();
    }
}

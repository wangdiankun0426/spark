package com.spark.llm.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.vo.ModelVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.llm.ModelDao;
import com.spark.dao.llm.ProviderDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.service.IModelService;
import com.spark.common.utils.CollectionUtil;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-14 09:49:10
 */
@Service
public class ModelServiceImpl extends BaseService<ModelQuery, ModelResult> implements IModelService {
    private final static Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private ProviderDao providerDao;

    /**
     * 创建模型
     * @param modelVO 模型数据
     * @return 创建结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.MODEL_INSERT)
    public ResultData<Void> createModel(ModelVO modelVO) {
        ResultData<Void> result = new ResultData<>();
        if (modelVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Model model = new Model();
        BeanUtil.copyProperties(modelVO, model);
        Long id = super.genObjectId(ObjectTypeEnum.MODEL);
        model.setId(id);
        int count = modelDao.insertDB(model);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        logger.info("model created with id={}, cache cleared", model.getId());
        result.setObjId(model.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改模型
     * @param modelVO 模型数据
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.MODEL_UPDATE)
    public ResultData<Void> updateModel(ModelVO modelVO) {
        ResultData<Void> result = new ResultData<>();
        if (modelVO == null || modelVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setId(modelVO.getId());
        ModelResult modelResult = modelDao.queryModel(modelQuery);
        if (modelResult == null) {
            result.setErrorCode(ErrorCodeEnum.MODEL_NOT_EXIST);
            return result;
        }
        Model model = new Model();
        BeanUtil.copyProperties(modelVO, model);
        int count = modelDao.updateDBById(model);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 清除模型缓存，确保下次获取时使用最新配置
        modelFactory.clearModelCache(modelVO.getId());
        logger.info("model updated with id={}, cache cleared", modelVO.getId());
        result.setObjId(modelVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除模型
     * @param modelVO 模型数据
     * @return  删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.MODEL_DELETE)
    public ResultData<Void> deleteModel(ModelVO modelVO) {
        ResultData<Void> result = new ResultData<>();
        if (modelVO == null || modelVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setId(modelVO.getId());
        ModelResult modelResult = modelDao.queryModel(modelQuery);
        if (modelResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Model model = new Model();
        model.setId(modelVO.getId());
        int count = modelDao.deleteDBById(model);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 清除模型缓存
        modelFactory.clearModelCache(modelVO.getId());
        logger.info("Model deleted with id={}, cache cleared", modelVO.getId());
        result.setObjId(modelVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询模型
     * @param query 查询模型条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<ModelResult>> pageModelList(ModelQuery query) {
        ResultData<PageResult<ModelResult>> result = new ResultData<>();
        if (query == null) {
            query = new ModelQuery();
        }
        PageResult<ModelResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询模型详情
     * @param query 查询模型条件
     * @return 详情
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.MODEL_DETAIL)
    public ResultData<ModelResult> queryModelDetail(ModelQuery query) {
        ResultData<ModelResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ModelResult modelResult = modelDao.queryModel(query);
        if (modelResult == null) {
            result.setErrorCode(ErrorCodeEnum.MODEL_NOT_EXIST);
            return result;
        }
        result.setData(modelResult);
        result.setObjId(modelResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<ModelResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        List<Long> providerIds = list.stream().map(ModelResult::getProviderId).distinct().toList();
        ProviderQuery providerQuery = new ProviderQuery();
        providerQuery.setIds(providerIds);
        providerQuery.setPage(false);
        List<ProviderResult> providerList = providerDao.queryProviderList(providerQuery);
        Map<Long, String> providerNameMap = providerList.stream().collect(Collectors.toMap(ProviderResult::getId, ProviderResult::getName));
        for (ModelResult modelResult : list) {
            modelResult.setProviderName(providerNameMap.get(modelResult.getProviderId()));
            modelResult.setTypeName(ModelTypeEnum.indexOf(modelResult.getType()).getDesc());
            modelResult.setStatusName(StatusEnum.indexOf(modelResult.getStatus()).getDesc());
        }

    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(ModelQuery query) {
        return modelDao.queryModelCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<ModelResult> queryList(ModelQuery query) {
        return modelDao.queryModelList(query);
    }

    /**
     * 查询模型最大id
     * @return 模型最大id
     */
    @Override
    protected Long queryMaxId() {
        return modelDao.queryModelMaxId();
    }
}

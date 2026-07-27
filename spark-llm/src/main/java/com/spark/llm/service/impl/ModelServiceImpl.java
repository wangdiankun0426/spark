package com.spark.llm.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.llm.entity.Model;
import com.spark.bean.llm.query.ProviderQuery;
import com.spark.bean.llm.query.ModelQuery;
import com.spark.bean.llm.result.ProviderResult;
import com.spark.bean.llm.result.ModelResult;
import com.spark.bean.llm.vo.ModelVO;
import com.spark.dao.llm.ModelDao;
import com.spark.dao.llm.ProviderDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ModelTypeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.llm.model.ModelFactory;
import com.spark.manage.BaseService;
import com.spark.llm.service.IModelService;
import com.spark.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
    public ResultData<Void> createModel(ModelVO modelVO) {
        ResultData<Void> result = new ResultData<>();
        if (modelVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Model model = new Model();
        BeanUtils.copyProperties(modelVO, model);
        Long id = super.genObjectId(ObjectTypeEnum.MODEL);
        model.setId(id);
        int count = modelDao.insertDB(model);
        if (count < 1) {
            logger.error("createModel error, insert db fail");
            return result;
        }
        logger.info("Model created with id: {}, cache cleared", model.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改模型
     * @param modelVO 模型数据
     * @return 修改结果
     */
    @Override
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
        BeanUtils.copyProperties(modelVO, model);
        int count = modelDao.updateDBById(model);
        if (count < 1) {
            logger.error("updateModel error, update db fail");
            return result;
        }
        // 清除模型缓存，确保下次获取时使用最新配置
        modelFactory.clearModelCache(modelVO.getId());
        logger.info("Model updated with id: {}, cache cleared", modelVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除模型
     * @param modelVO 模型数据
     * @return  删除结果
     */
    @Override
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
            logger.error("deleteModel error, delete db fail");
            return result;
        }
        // 清除模型缓存
        modelFactory.clearModelCache(modelVO.getId());
        logger.info("Model deleted with id: {}, cache cleared", modelVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询模型
     * @param query 查询模型条件
     * @return 分页结果
     */
    @Override
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

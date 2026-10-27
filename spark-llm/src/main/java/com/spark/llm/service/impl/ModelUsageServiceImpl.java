package com.spark.llm.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.query.ModelUsageQuery;
import com.spark.common.bean.llm.result.ModelUsageResult;
import com.spark.common.bean.llm.result.ModelUsageSummaryResult;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.ModelUsageSceneEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.dao.llm.ModelUsageDao;
import com.spark.dao.sys.UserDao;
import com.spark.llm.service.IModelUsageService;
import com.spark.manage.BaseService;
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
 * @since 2026/10/27 10:00:00
 */
@Service
@LogPrint
public class ModelUsageServiceImpl extends BaseService<ModelUsageQuery, ModelUsageResult> implements IModelUsageService {

    /**
     * 系统调用展示名
     */
    private static final String SYSTEM_USER_NAME = "系统";

    @Autowired
    private ModelUsageDao modelUsageDao;
    @Autowired
    private UserDao userDao;

    /**
     * 分页查询模型用量明细
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<ModelUsageResult>> pageUsageList(ModelUsageQuery query) {
        ResultData<PageResult<ModelUsageResult>> result = new ResultData<>();
        if (query == null) {
            query = new ModelUsageQuery();
        }
        PageResult<ModelUsageResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按模型类型汇总用量
     * @param query 查询条件
     * @return 汇总列表
     */
    @Override
    public ResultData<List<ModelUsageSummaryResult>> queryUsageSummary(ModelUsageQuery query) {
        ResultData<List<ModelUsageSummaryResult>> result = new ResultData<>();
        if (query == null) {
            query = new ModelUsageQuery();
        }
        this.applyTenantScope(query);
        List<ModelUsageSummaryResult> list = modelUsageDao.queryUsageSummary(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按日期与类型查询用量趋势
     * @param query 查询条件
     * @return 趋势列表
     */
    @Override
    public ResultData<List<ModelUsageTrendResult>> queryUsageTrend(ModelUsageQuery query) {
        ResultData<List<ModelUsageTrendResult>> result = new ResultData<>();
        if (query == null) {
            query = new ModelUsageQuery();
        }
        this.applyTenantScope(query);
        List<ModelUsageTrendResult> list = modelUsageDao.queryUsageTrend(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<ModelUsageResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        List<Long> userIds = list.stream()
                .map(ModelUsageResult::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userNameMap = Map.of();
        if (CollectionUtil.isNotEmpty(userIds)) {
            UserQuery userQuery = new UserQuery();
            userQuery.setIds(userIds);
            userQuery.setPage(false);
            List<UserResult> userList = userDao.queryUserList(userQuery);
            if (CollectionUtil.isNotEmpty(userList)) {
                userNameMap = userList.stream()
                        .collect(Collectors.toMap(UserResult::getId, UserResult::getName));
            }
        }
        for (ModelUsageResult item : list) {
            item.setTypeName(ModelTypeEnum.indexOf(item.getType()).getDesc());
            item.setSceneName(ModelUsageSceneEnum.indexOf(item.getScene()).getDesc());
            if (Long.valueOf(0L).equals(item.getTenantId())) {
                item.setUserName(SYSTEM_USER_NAME);
            } else {
                item.setUserName(userNameMap.get(item.getUserId()));
            }
        }
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(ModelUsageQuery query) {
        return modelUsageDao.queryUsageCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<ModelUsageResult> queryList(ModelUsageQuery query) {
        return modelUsageDao.queryUsageList(query);
    }

    /**
     * 非系统管理员按当前会话租户收口
     * @param query 查询参数
     */
    private void applyTenantScope(ModelUsageQuery query) {
        if (!SessionHolder.isSysAdmin()) {
            query.setTenantId(SessionHolder.getCurrentTenantId());
        }
    }
}

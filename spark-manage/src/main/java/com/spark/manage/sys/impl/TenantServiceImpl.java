package com.spark.manage.sys.impl;

import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Tenant;
import com.spark.common.bean.sys.query.TenantQuery;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.common.bean.sys.vo.TenantVO;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.RoleTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.dao.sys.*;
import com.spark.manage.BaseService;
import com.spark.manage.sys.ITenantService;
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
 * @since 2026-09-08 10:00:00
 * 租户服务
 */
@Service
@LogPrint
public class TenantServiceImpl extends BaseService<TenantQuery, TenantResult> implements ITenantService {
    private final static Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);
    @Autowired
    private TenantDao tenantDao;

    /**
     * 创建租户
     * @param tenantVO 租户参数
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_INSERT)
    public ResultData<Void> createTenant(TenantVO tenantVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantVO == null || StringUtil.isBlank(tenantVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantResult tenantResult = tenantDao.querySameNameTenant(tenantVO.getName());
        if (tenantResult != null) {
            result.setErrorCode(ErrorCodeEnum.TENANT_SAME_NAME_EXIST);
            return result;
        }
        Tenant tenant = new Tenant();
        Long tenantId = super.genObjectId(ObjectTypeEnum.TENANT);
        tenant.setId(tenantId);
        tenant.setName(tenantVO.getName());
        Integer status = tenantVO.getStatus();
        if (status == null) {
            status = StatusEnum.NORMAL.getValue();
        }
        tenant.setStatus(status);
        Integer accountCount = tenantVO.getAccountCount();
        if (accountCount == null) {
            accountCount = 10;
        }
        tenant.setAccountCount(accountCount);
        tenant.setDeadline(tenantVO.getDeadline());
        int count = tenantDao.insertDB(tenant);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(tenant.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询租户列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<TenantResult>> pageTenantList(TenantQuery query) {
        ResultData<PageResult<TenantResult>> result = new ResultData<>();
        if (query == null) {
            query = new TenantQuery();
        }
        PageResult<TenantResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改租户
     * @param tenantVO 租户参数
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_UPDATE)
    public ResultData<Void> updateTenant(TenantVO tenantVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantVO == null || tenantVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantResult tenantResult = tenantDao.querySameNameTenant(tenantVO.getName());
        if (tenantResult != null && !tenantResult.getId().equals(tenantVO.getId())) {
            result.setErrorCode(ErrorCodeEnum.TENANT_SAME_NAME_EXIST);
            return result;
        }
        Tenant tenant = new Tenant();
        tenant.setId(tenantVO.getId());
        tenant.setName(tenantVO.getName());
        tenant.setStatus(tenantVO.getStatus());
        Integer accountCount = tenantVO.getAccountCount();
        if (accountCount == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        tenant.setAccountCount(accountCount);
        tenant.setDeadline(tenantVO.getDeadline());
        int count = tenantDao.updateDBById(tenant);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除租户
     * @param tenantVO 租户参数
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_DELETE)
    public ResultData<Void> deleteTenant(TenantVO tenantVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantVO == null || tenantVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Tenant tenant = new Tenant();
        tenant.setId(tenantVO.getId());
        int count = tenantDao.deleteDBById(tenant);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 批量关闭超期租户
     * 状态为启用且截止时间已过的有效租户置为停用
     * @return 关闭结果
     */
    @Override
    public ResultData<Integer> closeExpiredTenant() {
        ResultData<Integer> result = new ResultData<>();
        int count = tenantDao.closeExpiredTenant();
        result.setData(count);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 租户列表
     */
    @Override
    protected void supplyList(List<TenantResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 总数
     */
    @Override
    protected int queryCount(TenantQuery query) {
        return tenantDao.queryTenantCount(query);
    }

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 租户列表
     */
    @Override
    protected List<TenantResult> queryList(TenantQuery query) {
        return tenantDao.queryTenantList(query);
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    @Override
    protected Long queryMaxId() {
        return tenantDao.queryTenantMaxId();
    }
}

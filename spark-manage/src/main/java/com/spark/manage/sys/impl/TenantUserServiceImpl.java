package com.spark.manage.sys.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.bean.sys.entity.User;
import com.spark.common.bean.sys.query.DepartmentQuery;
import com.spark.common.bean.sys.query.TenantQuery;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.common.bean.sys.vo.TenantVO;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.query.TenantUserQuery;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.TenantUserResult;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.TenantUserVO;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.RoleTypeEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.config.redis.RedisService;
import com.spark.dao.sys.*;
import com.spark.manage.BaseService;
import com.spark.manage.sys.ITenantUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 14:00:00
 * 租户用户服务
 */
@Service
@LogPrint
public class TenantUserServiceImpl extends BaseService<TenantUserQuery, TenantUserResult> implements ITenantUserService {
    private final static Logger logger = LoggerFactory.getLogger(TenantUserServiceImpl.class);
    @Autowired
    private TenantUserDao tenantUserDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TenantDao tenantDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private DepartmentDao departmentDao;

    /**
     * 添加用户到租户
     * @param tenantUserVO 租户用户参数
     * @return 添加结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_ADD_USER)
    public ResultData<Void> addUser(TenantUserVO tenantUserVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantUserVO == null || tenantUserVO.getTenantId() == null || CollectionUtil.isEmpty(tenantUserVO.getUserIds())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        result = this.checkTenantUserCount(tenantUserVO.getTenantId());
        BaseAssert.assertTrue(result);
        List<Long> userIds = tenantUserVO.getUserIds();
        Long tenantId = tenantUserVO.getTenantId();
        TenantUserQuery tenantUserQuery = new TenantUserQuery();
        tenantUserQuery.setTenantId(tenantId);
        tenantUserQuery.setUserIds(userIds);
        tenantUserQuery.setPage(false);
        List<TenantUserResult> existList = tenantUserDao.queryTenantUserList(tenantUserQuery);
        if (CollectionUtil.isNotEmpty(existList)) {
            List<Long> existUserIds = existList.stream().map(TenantUserResult::getUserId).toList();
            userIds = userIds.stream().filter(u -> !existUserIds.contains(u)).collect(Collectors.toList());
        }
        if (CollectionUtil.isEmpty(userIds)) {
            result.setCode(ResultData.OK);
            return result;
        }
        int count = tenantUserDao.batchInsertByTenantId(tenantId, userIds, SessionHolder.getCurrentUserId());
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 从租户移除用户
     * @param tenantUserVO 租户用户参数
     * @return 移除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_DEL_USER)
    public ResultData<Void> removeUser(TenantUserVO tenantUserVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantUserVO == null || tenantUserVO.getTenantId() == null || tenantUserVO.getUserId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int count = tenantUserDao.deleteTenantUser(tenantUserVO.getTenantId(), tenantUserVO.getUserId(), SessionHolder.getCurrentUserId());
        if (count < 0) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantUserVO.getTenantId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改租户用户角色类型
     * @param tenantUserVO 修改参数
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_UPDATE_USER_ROLE)
    public ResultData<Void> updateTenantUserRoleType(TenantUserVO tenantUserVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantUserVO == null || tenantUserVO.getTenantId() == null || tenantUserVO.getUserId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer roleType = tenantUserVO.getRoleType();
        if (!Objects.equals(roleType, RoleTypeEnum.COMMON.getValue()) && !Objects.equals(roleType, RoleTypeEnum.ORG_ADMIN.getValue())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int count = tenantUserDao.updateTenantUserRoleType(tenantUserVO.getTenantId(), tenantUserVO.getUserId(), roleType, SessionHolder.getCurrentUserId());
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantUserVO.getTenantId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询租户用户列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<TenantUserResult>> pageTenantUserList(TenantUserQuery query) {
        ResultData<PageResult<TenantUserResult>> result = new ResultData<>();
        if (query == null) {
            query = new TenantUserQuery();
        }
        PageResult<TenantUserResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询当前用户已加入且可用的租户列表
     * @return 租户列表
     */
    @Override
    public ResultData<List<TenantResult>> queryMyTenantList() {
        ResultData<List<TenantResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        TenantUserQuery tenantUserQuery = new TenantUserQuery();
        tenantUserQuery.setUserId(userId);
        tenantUserQuery.setPage(false);
        List<TenantUserResult> tenantUserList = tenantUserDao.queryTenantUserList(tenantUserQuery);
        List<TenantResult> list = new ArrayList<>();
        if (CollectionUtil.isEmpty(tenantUserList)) {
            result.setData(list);
            result.setCode(ResultData.OK);
            return result;
        }
        List<Long> tenantIds = tenantUserList.stream().map(TenantUserResult::getTenantId).distinct().collect(Collectors.toList());
        TenantQuery tenantQuery = new TenantQuery();
        tenantQuery.setIds(tenantIds);
        tenantQuery.setPage(false);
        tenantQuery.setStatus(StatusEnum.NORMAL.getValue());
        list = tenantDao.queryTenantList(tenantQuery);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 切换当前用户租户
     * @param tenantVO 租户参数
     * @return 切换结果
     */
    @Override
    public ResultData<Void> switchTenant(TenantVO tenantVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantVO == null || tenantVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        Long tenantId = tenantVO.getId();
        TenantQuery tenantQuery = new TenantQuery();
        tenantQuery.setId(tenantId);
        tenantQuery.setStatus(StatusEnum.NORMAL.getValue());
        TenantResult tenantResult = tenantDao.queryTenant(tenantQuery);
        if (tenantResult == null) {
            result.setErrorCode(ErrorCodeEnum.TENANT_UNAVAILABLE);
            return result;
        }
        TenantUserQuery tenantUserQuery = new TenantUserQuery();
        tenantUserQuery.setTenantId(tenantId);
        tenantUserQuery.setUserId(userId);
        TenantUserResult tenantUserResult = tenantUserDao.queryTenantUser(tenantUserQuery);
        if (tenantUserResult == null) {
            result.setErrorCode(ErrorCodeEnum.TENANT_UNAVAILABLE);
            return result;
        }
        User user = new User();
        user.setId(userId);
        user.setCurrentTenantId(tenantId);
        int count = userDao.updateDBById(user);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        boolean bo = this.resetCacheUserLoginInfo(tenantUserResult);
        if (!bo) {
            logger.error("reset cache user info fail!");
            return result;
        }
        result.setObjId(tenantId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询租户用户数量是否合规
     * @param tenantId
     * @return
     */
    @Override
    public ResultData<Void> checkTenantUserCount(Long tenantId) {
        ResultData<Void> result = new ResultData<>();
        if (tenantId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantQuery tenantQuery = new TenantQuery();
        tenantQuery.setTenantId(tenantId);
        TenantResult tenantResult = tenantDao.queryTenant(tenantQuery);
        if (tenantResult == null) {
            result.setErrorCode(ErrorCodeEnum.TENANT_NOT_EXIST);
            return result;
        }
        TenantUserQuery tenantUserQuery = new TenantUserQuery();
        tenantUserQuery.setTenantId(tenantId);
        int count = tenantUserDao.queryTenantUserCount(tenantUserQuery);
        if (tenantResult.getAccountCount() > count) {
            result.setCode(ResultData.OK);
            return result;
        }
        result.setErrorCode(ErrorCodeEnum.TENANT_USER_FULL);
        return result;
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 总数
     */
    @Override
    protected int queryCount(TenantUserQuery query) {
        return tenantUserDao.queryTenantUserCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<TenantUserResult> queryList(TenantUserQuery query) {
        return tenantUserDao.queryTenantUserList(query);
    }

    /**
     * 重置缓存用户登录信息
     * @param tenantUser 用户新租户数据
     * @return  缓存结果
     */
    private boolean resetCacheUserLoginInfo(TenantUserResult tenantUser) {
        String sessionId = SessionHolder.getCurrentSessionId();
        Session session = new Session();
        session.setSessionId(sessionId);
        Long tenantId = tenantUser.getTenantId();
        session.setTenantId(tenantId);
        Long userId = SessionHolder.getCurrentUserId();
        session.setUserId(userId);
        Long deptId = tenantUser.getDeptId();
        session.setDeptId(deptId);
        session.setAccountType(SessionHolder.getAccountType());
        session.setRoleType(tenantUser.getRoleType());
        int dataScope = roleDao.queryUserMaxDataScope(tenantId, userId);
        session.setDataScope(dataScope);
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setTenantId(tenantId);
        departmentQuery.setId(deptId);
        DepartmentResult department = departmentDao.queryDepartment(departmentQuery);
        if (department != null) {
            departmentQuery.setId(null);
            departmentQuery.setCode(department.getCode());
            List<DepartmentResult> departmentList = departmentDao.queryDepartmentList(departmentQuery);
            session.setDeptIds(StringUtil.join(departmentList.stream().map(DepartmentResult::getId).toList(), ","));
        }
        String sessionIdKey = ObjectCacheKey.LOGIN_SESSION + sessionId;
        return redisService.setStr(sessionIdKey, JsonUtil.toString(session), 60*60);
    }
}

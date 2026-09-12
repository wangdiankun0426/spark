package com.spark.manage.sys.impl;

import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.sys.query.RoleUserQuery;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.RoleUserResult;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.RoleUserVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.dao.sys.RoleUserDao;
import com.spark.dao.sys.UserDao;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.manage.sys.IRoleUserService;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
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
 * @since 2024/3/26 16:29
 */
@Service
@LogPrint
public class RoleUserServiceImpl extends BaseService<RoleUserQuery, RoleUserResult> implements IRoleUserService {
    @Autowired
    private RoleUserDao roleUserDao;
    @Autowired
    private UserDao userDao;

    /**
     * 添加用户
     * @param roleUserVO 角色用户数据
     * @return 结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ROLE_ADD_USER)
    public ResultData<Void> addUser(RoleUserVO roleUserVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleUserVO == null || roleUserVO.getRoleId() == null || CollectionUtil.isEmpty(roleUserVO.getUserIds())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        List<Long> userIds = roleUserVO.getUserIds();
        Long roleId = roleUserVO.getRoleId();
        RoleUserQuery roleUserQuery = new RoleUserQuery();
        roleUserQuery.setRoleId(roleId);
        roleUserQuery.setUserIds(userIds);
        roleUserQuery.setPage(false);
        List<RoleUserResult> existList = roleUserDao.queryRoleUserList(roleUserQuery);
        if (CollectionUtil.isNotEmpty(existList)) {
            List<Long> existUserIds = existList.stream().map(RoleUserResult::getUserId).toList();
            userIds = userIds.stream().filter(u -> !existUserIds.contains(u)).collect(Collectors.toList());
        }
        if (CollectionUtil.isEmpty(userIds)) {
            result.setCode(ResultData.OK);
            return result;
        }
        Long createdBy = SessionHolder.getCurrentUserId();
        Long tenantId = SessionHolder.getCurrentTenantId();
        int count = roleUserDao.batchInsertByRoleId(tenantId, roleId, userIds, createdBy);
        if (count < 1) {
            return result;
        }
        result.setObjId(roleId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 移除用户
     * @param roleUserVO 角色用户数据
     * @return 结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ROLE_DEL_USER)
    public ResultData<Void> removeUser(RoleUserVO roleUserVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleUserVO == null || roleUserVO.getRoleId() == null || roleUserVO.getUserId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int count = roleUserDao.deleteRoleUser(List.of(roleUserVO.getRoleId()), roleUserVO.getUserId(), SessionHolder.getCurrentUserId());
        if (count < 0) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(roleUserVO.getRoleId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询角色用户列表
     * @param query 查询参数
     * @return 结果
     */
    @Override
    public ResultData<PageResult<RoleUserResult>> pageRoleUserList(RoleUserQuery query) {
        ResultData<PageResult<RoleUserResult>> result = new ResultData<>();
        if (query == null) {
            query = new RoleUserQuery();
        }
        PageResult<RoleUserResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<RoleUserResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        List<Long> userIds = list.stream().map(RoleUserResult::getUserId).distinct().collect(Collectors.toList());
        UserQuery userQuery = new UserQuery();
        userQuery.setIds(userIds);
        userQuery.setPage(false);
        List<UserResult> userList = userDao.queryUserList(userQuery);
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }
        Map<Long, UserResult> userMap = userList.stream().collect(Collectors.toMap(UserResult::getId, v -> v));
        for (RoleUserResult roleUserResult : list) {
            Long userId = roleUserResult.getUserId();
            if (!userMap.containsKey(userId)) {
                continue;
            }
            UserResult userResult = userMap.get(userId);
            roleUserResult.setUserId(userResult.getId());
            roleUserResult.setUserName(userResult.getName());
            roleUserResult.setLoginName(userResult.getLoginName());
        }
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 总数
     */
    @Override
    protected int queryCount(RoleUserQuery query) {
        return roleUserDao.queryRoleUserCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<RoleUserResult> queryList(RoleUserQuery query) {
        return roleUserDao.queryRoleUserList(query);
    }
}

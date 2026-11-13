package com.spark.manage.sys.impl;

import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.sys.entity.Role;
import com.spark.common.bean.sys.query.RoleQuery;
import com.spark.common.bean.sys.result.RoleResult;
import com.spark.common.bean.sys.vo.RoleVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.dao.sys.RoleDao;
import com.spark.dao.sys.RoleDeptDao;
import com.spark.dao.sys.RoleMenuDao;
import com.spark.dao.sys.RoleUserDao;
import com.spark.config.redis.RedisService;
import com.spark.common.enums.DataScopeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.manage.sys.IRoleService;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
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
 * @since 2024/2/25 21:36
 */
@Service
@LogPrint
public class RoleServiceImpl extends BaseService<RoleQuery, RoleResult> implements IRoleService {
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private RoleMenuDao roleMenuDao;
    @Autowired
    private RoleDeptDao roleDeptDao;
    @Autowired
    private RoleUserDao roleUserDao;
    @Autowired
    private RedisService redisService;

    /**
     * 创建角色
     * @param roleVO 角色参数
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ROLE_INSERT)
    public ResultData<Void> createRole(RoleVO roleVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleVO == null || StringUtil.isBlank(roleVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (!SessionHolder.isOrgAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        RoleResult sameNameRole = roleDao.querySameNameRole(roleVO.getName(), tenantId);
        if (sameNameRole != null) {
            result.setErrorCode(ErrorCodeEnum.ROLE_SAME_NAME_EXIST);
            return result;
        }
        Long roleId = super.genObjectId(ObjectTypeEnum.ROLE);
        Role role = new Role();
        role.setName(roleVO.getName());
        role.setDataScope(roleVO.getDataScope());
        role.setStatus(roleVO.getStatus());
        Integer menuCheckStrictly = roleVO.getMenuCheckStrictly() == null ? 1 : roleVO.getMenuCheckStrictly();
        role.setMenuCheckStrictly(menuCheckStrictly);
        role.setId(roleId);
        int count = roleDao.insertDB(role);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        this.saveRoleRelations(roleId, tenantId, roleVO);
        result.setObjId(roleId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 结果
     */
    @Override
    public ResultData<PageResult<RoleResult>> pageRoleList(RoleQuery query) {
        ResultData<PageResult<RoleResult>> result = new ResultData<>();
        if (query == null) {
            query = new RoleQuery();
        }
        PageResult<RoleResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询角色详情，含授权菜单与自定义部门勾选
     * @param query 查询参数
     * @return 角色详情
     */
    @Override
    public ResultData<RoleResult> queryRoleDetail(RoleQuery query) {
        ResultData<RoleResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long roleId = query.getId();
        if (!this.validateRoleManage(roleId)) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        query.setTenantId(tenantId);
        RoleResult roleResult = roleDao.queryRole(query);
        if (roleResult == null) {
            result.setErrorCode(ErrorCodeEnum.ROLE_NOT_EXIST);
            return result;
        }
        List<Long> menuIds = roleMenuDao.queryMenuIdsByRoleId(roleId);
        roleResult.setMenuIds(menuIds);
        List<Long> deptIds = roleDeptDao.queryDeptIdsByRoleId(roleId);
        roleResult.setDeptIds(deptIds);
        result.setData(roleResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改角色
     * @param roleVO 修改的参数
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ROLE_UPDATE)
    public ResultData<Void> updateRole(RoleVO roleVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleVO == null || roleVO.getId() == null || StringUtil.isBlank(roleVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long roleId = roleVO.getId();
        if (!this.validateRoleManage(roleId)) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        RoleResult sameNameRole = roleDao.querySameNameRole(roleVO.getName(), tenantId);
        if (sameNameRole != null && !sameNameRole.getId().equals(roleId)) {
            result.setErrorCode(ErrorCodeEnum.ROLE_SAME_NAME_EXIST);
            return result;
        }
        Role role = new Role();
        role.setId(roleId);
        role.setName(roleVO.getName());
        role.setDataScope(roleVO.getDataScope());
        role.setStatus(roleVO.getStatus());
        Integer menuCheckStrictly = roleVO.getMenuCheckStrictly() == null ? 1 : roleVO.getMenuCheckStrictly();
        role.setMenuCheckStrictly(menuCheckStrictly);
        int count = roleDao.updateDBById(role);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        this.saveRoleRelations(roleId, tenantId, roleVO);
        this.invalidateRoleUserSession(roleId);
        result.setObjId(roleId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除角色
     * @param roleVO 删除参数
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ROLE_DELETE)
    public ResultData<Void> deleteRole(RoleVO roleVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleVO == null || roleVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long roleId = roleVO.getId();
        if (!this.validateRoleManage(roleId)) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        Role role = new Role();
        role.setId(roleId);
        int count = roleDao.deleteDBById(role);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        roleMenuDao.deleteByRoleId(roleId);
        roleDeptDao.deleteByRoleId(roleId);
        this.invalidateRoleUserSession(roleId);
        result.setObjId(roleId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<RoleResult> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        for (RoleResult roleResult : list) {
            roleResult.setDataScopeName(DataScopeEnum.indexOf(roleResult.getDataScope()).getDesc());
        }
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 查询总数
     */
    @Override
    protected int queryCount(RoleQuery query) {
        return roleDao.queryRoleCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<RoleResult> queryList(RoleQuery query) {
        return roleDao.queryRoleList(query);
    }

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() {
        return roleDao.queryRoleMaxId();
    }

    /**
     * 重授权角色菜单与部门关联，物理删除后批量插入
     * @param roleId 角色id
     * @param tenantId 租户id
     * @param roleVO 角色参数
     */
    private void saveRoleRelations(Long roleId, Long tenantId, RoleVO roleVO) {
        Long createdBy = SessionHolder.getCurrentUserId();
        roleMenuDao.deleteByRoleId(roleId);
        List<Long> menuIds = roleVO.getMenuIds();
        if (CollectionUtil.isNotEmpty(menuIds)) {
            roleMenuDao.batchInsertByRoleId(tenantId, roleId, menuIds, createdBy);
        }
        roleDeptDao.deleteByRoleId(roleId);
        // 自定义数据范围才保存部门关联
        if (DataScopeEnum.CUSTOM.getValue().equals(roleVO.getDataScope())) {
            List<Long> deptIds = roleVO.getDeptIds();
            if (CollectionUtil.isNotEmpty(deptIds)) {
                roleDeptDao.batchInsertByRoleId(tenantId, roleId, deptIds, createdBy);
            }
        }
    }

    /**
     * 校验角色管理权限与角色归属，仅能操作本租户的角色
     * @param roleId 角色id
     * @return true 有权限，false 无权限或角色不存在
     */
    private boolean validateRoleManage(Long roleId) {
        if (!SessionHolder.isOrgAdmin()) {
            return false;
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        RoleQuery roleQuery = new RoleQuery();
        roleQuery.setId(roleId);
        roleQuery.setTenantId(tenantId);
        RoleResult roleResult = roleDao.queryRole(roleQuery);
        return roleResult != null;
    }

    /**
     * 角色变更后定向失效关联用户的登录会话
     * @param roleId 角色id
     */
    private void invalidateRoleUserSession(Long roleId) {
        if (roleId == null) {
            return;
        }
        List<Long> userIds = roleUserDao.queryUserIdsByRoleId(roleId);
        if (CollectionUtil.isEmpty(userIds)) {
            return;
        }
        for (Long userId : userIds) {
            redisService.deleteLoginSessionByUserId(userId);
        }
    }
}

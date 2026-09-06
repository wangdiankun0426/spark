package com.spark.manage.sys.impl;

import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.common.bean.sys.entity.Role;
import com.spark.common.bean.sys.query.RoleQuery;
import com.spark.common.bean.sys.result.RoleResult;
import com.spark.common.bean.sys.vo.RoleVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.dao.sys.RoleDao;
import com.spark.common.enums.DataScopeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.manage.sys.IRoleService;
import com.spark.manage.BaseService;
import com.spark.common.utils.StringUtil;
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
 * @since 2024/2/25 21:36
 */
@Service
public class RoleServiceImpl extends BaseService<RoleQuery, RoleResult> implements IRoleService {
    private final static Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
    @Autowired
    private RoleDao roleDao;

    /**
     * 创建角色
     * @param roleVO 角色参数
     * @return 创建结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.ROLE_INSERT)
    public ResultData<Void> createRole(RoleVO roleVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleVO == null || StringUtil.isBlank(roleVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        RoleResult roleResult = roleDao.querySameNameRole(roleVO.getName());
        if (roleResult != null) {
            result.setErrorCode(ErrorCodeEnum.ROLE_SAME_NAME_EXIST);
            return result;
        }
        Long roleId = super.genObjectId(ObjectTypeEnum.ROLE);
        Role role = new Role();
        role.setName(roleVO.getName());
        role.setDataScope(roleVO.getDataScope());
        role.setStatus(roleVO.getStatus());
        String menuIds = this.buildMenuIds(roleVO);
        role.setMenuIds(menuIds);
        role.setId(roleId);
        int count = roleDao.insertDB(role);
        if (count < 1) {
            return result;
        }
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
     * 修改角色
     * @param roleVO 修改的参数
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.ROLE_UPDATE)
    public ResultData<Void> updateRole(RoleVO roleVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleVO == null || roleVO.getId() == null || StringUtil.isBlank(roleVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        RoleResult roleResult = roleDao.querySameNameRole(roleVO.getName());
        if (roleResult != null && !roleResult.getId().equals(roleVO.getId())) {
            result.setErrorCode(ErrorCodeEnum.ROLE_SAME_NAME_EXIST);
            return result;
        }
        Role role = new Role();
        role.setId(roleVO.getId());
        role.setName(roleVO.getName());
        role.setDataScope(roleVO.getDataScope());
        role.setStatus(roleVO.getStatus());
        String menuIds = this.buildMenuIds(roleVO);
        role.setMenuIds(menuIds);
        int count = roleDao.updateDBById(role);
        if (count < 1) {
            return result;
        }
        result.setObjId(roleVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除角色
     * @param roleVO 删除参数
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.ROLE_DELETE)
    public ResultData<Void> deleteRole(RoleVO roleVO) {
        ResultData<Void> result = new ResultData<>();
        if (roleVO == null || roleVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Role role = new Role();
        role.setId(roleVO.getId());
        int count = roleDao.deleteDBById(role);
        if (count < 1) {
            return result;
        }
        result.setObjId(roleVO.getId());
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
     * 拼接角色菜单ID为逗号字符串
     * @param roleVO 角色参数
     * @return 逗号分隔的菜单ID字符串
     */
    private String buildMenuIds(RoleVO roleVO) {
        if (roleVO == null) {
            return null;
        }
        List<Long> menuIdList = roleVO.getMenuIds();
        if (menuIdList == null || menuIdList.isEmpty()) {
            return null;
        }
        String menuIds = StringUtil.join(menuIdList, ",");
        return menuIds;
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
}

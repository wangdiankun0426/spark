package com.spark.manage;

import com.spark.bean.llm.query.AgentQuery;
import com.spark.bean.llm.query.ModelQuery;
import com.spark.bean.llm.result.ModelResult;
import com.spark.bean.system.query.DepartmentQuery;
import com.spark.bean.system.query.RoleQuery;
import com.spark.bean.llm.result.AgentResult;
import com.spark.bean.system.result.DepartmentResult;
import com.spark.bean.system.result.RoleResult;
import com.spark.bean.system.result.UserResult;
import com.spark.bean.base.BaseQuery;
import com.spark.bean.base.BaseResult;
import com.spark.bean.base.PageResult;
import com.spark.config.redis.RedisService;
import com.spark.constant.ObjectCacheKey;
import com.spark.dao.llm.AgentDao;
import com.spark.dao.llm.ModelDao;
import com.spark.dao.system.*;
import com.spark.bean.system.query.UserQuery;
import com.spark.enums.ObjectTypeEnum;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
 * @since 2024/2/27 22:03
 */
public abstract class BaseService<Q extends BaseQuery, R extends BaseResult> {
    @Autowired
    private UserDao userDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private AgentDao agentDao;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private RedisService redisService;

    /**
     * 分页查询
     * @param query 查询参数
     * @return 分页结果
     */
    protected PageResult<R> pageList(Q query) {
        PageResult<R> pageResult = new PageResult<>();
        int count = this.queryCount(query);
        List<R> list = this.queryList(query);
        this.supplyList(list);
        pageResult.setRows(list);
        pageResult.setTotal(count);
        return pageResult;
    }

    /**
     * 补充列表
     * @param list 列表
     */
    protected void supplyList(List<R> list) {
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 总数
     */
    protected int queryCount(Q query){
        return 0;
    }

    /**
     * 分页列表
     * @param query 查询参数
     * @return 列表
     */
    protected List<R> queryList(Q query){
        return new ArrayList<>();
    }

    /**
     * 补充创建人名称
     * @param r 结果
     */
    protected void supplyCreatedByName(R r) {
        if (r == null || r.getCreatedBy() == null) {
            return;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setId(r.getCreatedBy());
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult == null) {
            return;
        }
        r.setCreatedByName(userResult.getName());
    }

    /**
     * 补充创建人名称
     * @param list 列表
     */
    protected void supplyCreatedByName(List<R> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = list.stream().map(BaseResult::getCreatedBy).collect(Collectors.toList());
        UserQuery userQuery = new UserQuery();
        userQuery.setPage(false);
        userQuery.setIds(userIds);
        List<UserResult> userList = userDao.queryUserList(userQuery);
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }
        Map<Long, String> userNameMap = userList.stream().collect(Collectors.toMap(UserResult::getId, UserResult::getName));
        for (BaseResult baseResult : list) {
            Long createdBy = baseResult.getCreatedBy();
            if (!userNameMap.containsKey(createdBy)) {
                continue;
            }
            baseResult.setCreatedByName(userNameMap.get(createdBy));
        }
    }

    /**
     * 补充修改人名称
     * @param r 结果
     */
    protected void supplyUpdatedByName(R r) {
        if (r == null || r.getUpdatedBy() == null) {
            return;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setId(r.getUpdatedBy());
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult == null) {
            return;
        }
        r.setUpdatedByName(userResult.getName());
    }

    /**
     * 补充修改人名称
     * @param list 列表
     */
    protected void supplyUpdatedByName(List<R> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = list.stream().map(BaseResult::getUpdatedBy).collect(Collectors.toList());
        UserQuery userQuery = new UserQuery();
        userQuery.setPage(false);
        userQuery.setIds(userIds);
        List<UserResult> userList = userDao.queryUserList(userQuery);
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }
        Map<Long, String> userNameMap = userList.stream().collect(Collectors.toMap(UserResult::getId, UserResult::getName));
        for (BaseResult baseResult : list) {
            Long createdBy = baseResult.getCreatedBy();
            if (!userNameMap.containsKey(createdBy)) {
                continue;
            }
            baseResult.setUpdatedByName(userNameMap.get(createdBy));
        }
    }

    /**
     * 获取对象id名称 用户/部门/角色
     * @param objIds 对象id
     * @return 列表
     */
    protected List<String> getObjNames(List<Long> objIds) {
        List<String> list = new ArrayList<>();
        if (CollectionUtil.isEmpty(objIds)) {
            return list;
        }
        objIds.forEach(objId -> {
            String objName = this.getObjName(objId);
            list.add(objName);
        });
        return list;
    }

    /**
     * 获取对象id名称 用户/部门/角色
     * @param objId 对象id
     * @return 对象名称
     */
    protected String getObjName(Long objId) {
        String name = "未知对象";
        ObjectTypeEnum typeEnum = this.getObjEnum(objId);
        if (typeEnum.equals(ObjectTypeEnum.USER)) {
            UserQuery userQuery = new UserQuery();
            userQuery.setId(objId);
            UserResult userResult = userDao.queryUser(userQuery);
            name = userResult.getName();
        } else if (typeEnum.equals(ObjectTypeEnum.DEPARTMENT)) {
            DepartmentQuery departmentQuery = new DepartmentQuery();
            departmentQuery.setId(objId);
            DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
            name = departmentResult.getName();
        } else if (typeEnum.equals(ObjectTypeEnum.ROLE)) {
            RoleQuery roleQuery = new RoleQuery();
            roleQuery.setId(objId);
            RoleResult roleResult = roleDao.queryRole(roleQuery);
            name = roleResult.getName();
        } else if (typeEnum.equals(ObjectTypeEnum.AGENT)) {
            AgentQuery agentQuery = new AgentQuery();
            agentQuery.setId(objId);
            AgentResult agentResult = agentDao.queryAgent(agentQuery);
            name = agentResult.getName();
        } else if (typeEnum.equals(ObjectTypeEnum.MODEL)) {
            ModelQuery modelQuery = new ModelQuery();
            modelQuery.setId(objId);
            ModelResult modelResult = modelDao.queryModel(modelQuery);
            name = modelResult.getName();
        }
        return name;
    }

    /**
     * 获取对象枚举
     * @param objId 对象id
     * @return 对象枚举
     */
    public ObjectTypeEnum getObjEnum(Long objId) {
        //获取对象类型
        ObjectTypeEnum objType = ObjectTypeEnum.UNKNOWN;
        if(objId != null) {
            Integer intObjType = Integer.valueOf(objId % 100+"");
            objType = ObjectTypeEnum.indexOf(intObjType);
        }
        return objType;
    }

    /**
     * 构造obj id
     * @param objectType 对象类型
     * @return obj id
     */
    public Long genObjectId(ObjectTypeEnum objectType) {
        String key = ObjectCacheKey.SEQUENCE_PRIMARY + objectType.getValue();
        String value = redisService.getValue(key);
        Long id;
        if (StringUtil.isBlank(value)) {
            id = this.queryMaxId();
            if (id == null || id == 0L) {
                id = 100L;
            } else {
                id = id/100*100;
            }
            redisService.setObj(key, id);
        }
        id = redisService.incr(key,100);
        id = id+objectType.getValue();
        return id;
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    protected Long queryMaxId(){
        return 0L;
    }
}

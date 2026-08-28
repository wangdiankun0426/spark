package com.spark.manage.system.impl;

import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.bean.system.result.UserResult;
import com.spark.bean.system.vo.DepartmentVO;
import com.spark.constant.ObjectCacheKey;
import com.spark.bean.system.entity.Department;
import com.spark.bean.system.query.DepartmentQuery;
import com.spark.bean.system.query.UserQuery;
import com.spark.bean.system.result.DepartmentResult;
import com.spark.bean.system.tree.DepartmentTree;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.dao.system.DepartmentDao;
import com.spark.dao.system.UserDao;
import com.spark.enums.OperateTypeEnum;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.manage.BaseService;
import com.spark.manage.system.IDepartmentService;
import com.spark.config.redis.RedisService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import com.spark.utils.TreeUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.utils.BeanUtil;
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
 * @since 2024/2/17 19:39
 */
@Service
public class DepartmentServiceImpl extends BaseService<DepartmentQuery, DepartmentResult> implements IDepartmentService {
    private final static Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserDao userDao;

    /**
     * 创建部门
     * @param departmentVO 部门信息
     * @return 结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DEPT_INSERT)
    public ResultData<Void> createDepartment(DepartmentVO departmentVO) {
        ResultData<Void> result = this.validateCreateDepartmentParam(departmentVO);
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        Department department = new Department();
        long deptId = super.genObjectId(ObjectTypeEnum.DEPARTMENT);
        department.setId(deptId);
        department.setName(departmentVO.getName());
        department.setPrtId(departmentVO.getPrtId());
        department.setHeaderId(departmentVO.getHeaderId());
        department.setDeptNum(departmentVO.getDeptNum());
        department.setStatus(departmentVO.getStatus());
        department.setOrderNum(departmentVO.getOrderNum());
        department.setWecomId(departmentVO.getWecomId());
        this.supplyDepartmentCode(department);
        if (department.getStatus() == null) {
            department.setStatus(StatusEnum.NORMAL.getValue());
        }
        int count = departmentDao.insertDB(department);
        if (count < 1) {
            return result;
        }
        result.setObjId(deptId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询部门树
     * @return 部门树
     */
    @Override
    public ResultData<List<DepartmentTree>> queryDepartmentTree() {
        ResultData<List<DepartmentTree>> result = new ResultData<>();
        DepartmentQuery query = new DepartmentQuery();
        query.setPage(false);
        List<DepartmentResult> departmentList = departmentDao.queryDepartmentList(query);
        if (CollectionUtil.isEmpty(departmentList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        this.supplyDepartmentList(departmentList);
        List<DepartmentTree> treeList = new ArrayList<>();
        for (DepartmentResult departmentResult : departmentList) {
            DepartmentTree departmentTree = new DepartmentTree();
            BeanUtil.copyProperties(departmentResult, departmentTree);
            treeList.add(departmentTree);
        }
        List<DepartmentTree> list = new TreeUtil<DepartmentTree>().convertTreeList(treeList);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改部门
     * @param departmentVO 部门信息
     * @return 结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DEPT_UPDATE)
    public ResultData<Void> updateDepartment(DepartmentVO departmentVO) {
        ResultData<Void> result = this.validateUpdateDepartmentParam(departmentVO);
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        Department department = new Department();
        department.setId(departmentVO.getId());
        department.setName(departmentVO.getName());
        department.setPrtId(departmentVO.getPrtId());
        department.setHeaderId(departmentVO.getHeaderId());
        department.setDeptNum(departmentVO.getDeptNum());
        department.setStatus(departmentVO.getStatus());
        department.setOrderNum(departmentVO.getOrderNum());
        department.setWecomId(departmentVO.getWecomId());
        // prtId变化时重新生成部门层级码
        if (departmentVO.getPrtId() != null) {
            DepartmentQuery prtQuery = new DepartmentQuery();
            prtQuery.setId(departmentVO.getId());
            DepartmentResult oldDept = departmentDao.queryDepartment(prtQuery);
            if (oldDept != null && !Objects.equals(oldDept.getPrtId(), departmentVO.getPrtId())) {
                this.supplyDepartmentCode(department);
            }
        }
        int count = departmentDao.updateDBById(department);
        if (count < 1) {
            return result;
        }
        result.setObjId(departmentVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除部门
     * @param departmentVO 部门信息
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DEPT_DELETE)
    public ResultData<Void> deleteDepartment(DepartmentVO departmentVO) {
        ResultData<Void> result = new ResultData<>();
        if (departmentVO == null || departmentVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setId(departmentVO.getId());
        DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
        if (departmentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DEPT_NOT_EXIST);
            return result;
        }
        int count = departmentDao.deleteSubDepartment(departmentResult.getCode(), SessionHolder.getCurrentUserId());
        if (count < 0) {
            return result;
        }
        count = userDao.resetUserDeptByDeptId(departmentVO.getId());
        if (count < 0) {
            return result;
        }
        result.setObjId(departmentVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充部门全路径
     * @param departmentResult 部门信息
     * @return 部门信息
     */
    @Override
    public ResultData<String> supplyDeptPath(DepartmentResult departmentResult) {
        ResultData<String> result = new ResultData<>();
        if (departmentResult == null || StringUtil.isBlank(departmentResult.getCode())) {
            return result;
        }
        List<String> codes = this.analyzeCode(departmentResult.getCode());
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setCodes(codes);
        List<DepartmentResult> departmentList = departmentDao.queryDepartmentList(departmentQuery);
        if (CollectionUtil.isEmpty(departmentList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        String deptPath = departmentList.stream().map(DepartmentResult::getName).collect(Collectors.joining("/"));
        result.setData(deptPath);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充部门的code码
     * @param dept  部门
     */
    private void supplyDepartmentCode(Department dept) {
        String code = "";
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setId(dept.getPrtId());
        DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);

        if(departmentResult != null && StringUtil.isNotBlank(departmentResult.getCode())) {
            code = departmentResult.getCode();
        }
        String orgCode = this.genDepartmentCode(dept.getPrtId());
        // 根使用当前值
        if (dept.getPrtId() == 0L) {
            code = orgCode;
        } else {
            // 非根部门使用父级值加上当前值
            code = code + orgCode;
        }
        dept.setCode(code);
    }

    /**
     * 获取部门code
     * @param deptId 部门id
     * @return 部门code
     */
    private String genDepartmentCode(Long deptId) {
        if(deptId == null){
            return null;
        }
        String key = ObjectCacheKey.DEPT_CODE + deptId;
        long id = redisService.incr(key,1);
        String code = String.format("%03d", id);
        if ("001".equals(code)) {
            String deptCode = departmentDao.getMaxCode(deptId);
            // 不是001 需要根据父级code重新计数
            if(StringUtil.isNotBlank(deptCode)) {
                deptCode = deptCode.substring(deptCode.length()-3);
                this.updateDepartmentCode(deptId, deptCode);
                id = redisService.incr(key,1);
                code = String.format("%03d", id);
            }
        }
        return code;
    }

    /**
     * 解析code
     * @param var 部门code
     * @return 部门code
     */
    private List<String> analyzeCode(String var) {
        List<String> codes = new ArrayList<>();
        for (int i = var.length(); i > 0; i -= 3) {
            String code = var.substring(0, i);
            codes.add(code);
        }
        return codes;
    }

    /**
     * 更新部门code
     * @param deptId 部门id
     * @param deptCode 部门code
     */
    public void updateDepartmentCode(Long deptId, String deptCode) {
        if(!NumberUtils.isDigits(deptCode)) {
            return;
        }
        int code = Integer.parseInt(deptCode);
        String key = ObjectCacheKey.DEPT_CODE + deptId;
        redisService.setObj(key, code);
    }

    /**
     * 校验创建部门参数
     * @param departmentVO 部门信息
     * @return 校验结果
     */
    private ResultData<Void> validateCreateDepartmentParam(DepartmentVO departmentVO) {
        ResultData<Void> result = new ResultData<>();
        if(departmentVO == null || StringUtil.isBlank(departmentVO.getName())){
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 根部门
        if (departmentVO.getPrtId() == null){
            departmentVO.setPrtId(0L);
        }
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setName(departmentVO.getName());
        departmentQuery.setPrtId(departmentVO.getPrtId());
        DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
        if (departmentResult != null) {
            result.setErrorCode(ErrorCodeEnum.DEPT_SAME_NAME_ALREADY_EXIST);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    private void supplyDepartmentList(List<DepartmentResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        List<Long> headerIds = list.stream().map(DepartmentResult::getHeaderId).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(headerIds)) {
            return;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setIds(headerIds);
        List<UserResult> headerList = userDao.queryUserList(userQuery);
        if (CollectionUtil.isEmpty(headerList)) {
            return;
        }
        Map<Long, String> headerNameMap = headerList.stream().collect(Collectors.toMap(UserResult::getId, UserResult::getName, (v1, v2) -> v2));
        for (DepartmentResult departmentResult : list) {
            if (departmentResult.getHeaderId() == null || !headerNameMap.containsKey(departmentResult.getHeaderId())) {
                continue;
            }
            departmentResult.setHeaderName(headerNameMap.get(departmentResult.getHeaderId()));
        }
    }

    /**
     * 创建修改部门参数
     * @param departmentVO 部门信息
     * @return 校验结果
     */
    private ResultData<Void> validateUpdateDepartmentParam(DepartmentVO departmentVO) {
        ResultData<Void> result = new ResultData<>();
        if (departmentVO == null || departmentVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(departmentVO.getName())) {
            result.setCode(ResultData.OK);
            return result;
        }
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setName(departmentVO.getName());
        DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
        if (departmentResult != null && !departmentResult.getId().equals(departmentVO.getId())) {
            result.setErrorCode(ErrorCodeEnum.DEPT_SAME_NAME_ALREADY_EXIST);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询部门最大id
     * @return 部门最大id
     */
    @Override
    protected Long queryMaxId() {
        return departmentDao.queryDeptMaxId();
    }
}

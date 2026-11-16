package com.spark.manage.sys.impl;

import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.DepartmentVO;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.bean.sys.entity.Department;
import com.spark.common.bean.sys.entity.DepartmentProfile;
import com.spark.common.bean.sys.query.DepartmentQuery;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.tree.DepartmentTree;
import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.dao.sys.DepartmentDao;
import com.spark.dao.sys.DepartmentProfileDao;
import com.spark.dao.sys.UserDao;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.manage.BaseService;
import com.spark.manage.sys.IDepartmentService;
import com.spark.config.redis.RedisService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.common.utils.TreeUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@LogPrint
public class DepartmentServiceImpl extends BaseService<DepartmentQuery, DepartmentResult> implements IDepartmentService {
    private final static Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private DepartmentProfileDao departmentProfileDao;
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
    @LogOperate(operateType = OperateTypeEnum.DEPT_INSERT)
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
    public ResultData<Void> createDepartment(DepartmentVO departmentVO) {
        ResultData<Void> result = this.validateCreateDepartmentParam(departmentVO);
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        if (!SessionHolder.isOrgAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
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
        this.supplyDepartmentCode(department);
        if (department.getStatus() == null) {
            department.setStatus(StatusEnum.NORMAL.getValue());
        }
        int count = departmentDao.insertDB(department);
        if (count < 1) {
            throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
        }
        DepartmentProfile departmentProfile = new DepartmentProfile();
        departmentProfile.setId(deptId);
        departmentProfile.setWecomId(departmentVO.getWecomId());
        count = departmentProfileDao.insertDB(departmentProfile);
        if (count < 1) {
            throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
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
        query.setTenantId(SessionHolder.getCurrentTenantId());
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
    @LogOperate(operateType = OperateTypeEnum.DEPT_UPDATE)
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
    public ResultData<Void> updateDepartment(DepartmentVO departmentVO) {
        ResultData<Void> result = this.validateUpdateDepartmentParam(departmentVO);
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        Long deptId = departmentVO.getId();
        if (!this.validateDeptManage(deptId)) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        Department department = new Department();
        department.setId(deptId);
        department.setName(departmentVO.getName());
        department.setPrtId(departmentVO.getPrtId());
        department.setHeaderId(departmentVO.getHeaderId());
        department.setDeptNum(departmentVO.getDeptNum());
        department.setStatus(departmentVO.getStatus());
        department.setOrderNum(departmentVO.getOrderNum());
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
            throw new BaseException(ErrorCodeEnum.UPDATE_DATA_FAIL);
        }
        this.upsertDepartmentProfile(departmentVO.getId(), departmentVO.getWecomId());
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
    @LogOperate(operateType = OperateTypeEnum.DEPT_DELETE)
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
    public ResultData<Void> deleteDepartment(DepartmentVO departmentVO) {
        ResultData<Void> result = new ResultData<>();
        if (departmentVO == null || departmentVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long deptId = departmentVO.getId();
        if (!this.validateDeptManage(deptId)) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setId(deptId);
        DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
        if (departmentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DEPT_NOT_EXIST);
            return result;
        }
        int count = departmentDao.deleteSubDepartment(departmentResult.getCode(), SessionHolder.getCurrentUserId());
        if (count < 1) {
            throw new BaseException(ErrorCodeEnum.DELETE_DATA_FAIL);
        }
        count = departmentProfileDao.deleteByDeptCode(departmentResult.getCode(), SessionHolder.getCurrentUserId());
        if (count < 0) {
            throw new BaseException(ErrorCodeEnum.DELETE_DATA_FAIL);
        }
        count = userDao.resetUserDeptByDeptId(departmentVO.getId());
        if (count < 0) {
            throw new BaseException(ErrorCodeEnum.DELETE_DATA_FAIL);
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
        if (departmentResult != null && StringUtil.isNotBlank(departmentResult.getCode())) {
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
     * 校验部门管理权限与部门归属，仅能操作本租户的部门
     * @param deptId 部门id
     * @return true 有权限，false 无权限或部门不存在
     */
    private boolean validateDeptManage(Long deptId) {
        if (!SessionHolder.isOrgAdmin()) {
            return false;
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setId(deptId);
        departmentQuery.setTenantId(tenantId);
        DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
        return departmentResult != null;
    }

    /**
     * 维护部门扩展信息
     * 无扩展记录且需写企业微信ID时新增，已有记录则更新企业微信ID
     * @param deptId 部门id
     * @param wecomId 企业微信部门id
     */
    private void upsertDepartmentProfile(Long deptId, Long wecomId) {
        DepartmentProfile existProfile = departmentProfileDao.queryByDeptId(deptId);
        if (existProfile == null) {
            if (wecomId == null) {
                return;
            }
            DepartmentProfile departmentProfile = new DepartmentProfile();
            departmentProfile.setId(deptId);
            departmentProfile.setWecomId(wecomId);
            int count = departmentProfileDao.insertDB(departmentProfile);
            if (count < 1) {
                throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
            }
            return;
        }
        if (wecomId == null) {
            return;
        }
        DepartmentProfile departmentProfile = new DepartmentProfile();
        departmentProfile.setId(deptId);
        departmentProfile.setWecomId(wecomId);
        int count = departmentProfileDao.updateDBById(departmentProfile);
        if (count < 1) {
            throw new BaseException(ErrorCodeEnum.UPDATE_DATA_FAIL);
        }
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

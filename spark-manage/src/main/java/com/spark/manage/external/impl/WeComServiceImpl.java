package com.spark.manage.external.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.UserProfile;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.vo.DepartmentVO;
import com.spark.common.bean.sys.vo.UserVO;
import com.spark.common.enums.TenantConfigEnum;
import com.spark.config.wecom.response.WeComDeptListRes;
import com.spark.config.wecom.response.WeComUserListRes;
import com.spark.dao.sys.DepartmentDao;
import com.spark.dao.sys.UserProfileDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.manage.external.IWeComService;
import com.spark.manage.sys.IDepartmentService;
import com.spark.manage.sys.ITenantConfigService;
import com.spark.manage.sys.IUserService;
import com.spark.config.wecom.response.WeComMsg;
import com.spark.common.enums.WeComMsgTypeEnum;
import com.spark.config.wecom.WeComUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.XmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 10:02:00
 * 企业微信服务实现类
 */
@Service
public class WeComServiceImpl implements IWeComService {
    private final static Logger logger = LoggerFactory.getLogger(WeComServiceImpl.class);
    @Autowired
    private WeComUtil weComUtil;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ITenantConfigService tenantConfigService;

    /**
     * 处理企业微信消息
     * @param msg 解密后的 XML 消息内容
     * @return 回复消息内容（为空字符串表示不回复）
     */
    @Override
    public ResultData<String> handleWeComMsg(String msg) {
        ResultData<String> result = new ResultData<>();
        if(StringUtils.isBlank(msg)) {
            return result;
        }
        try {
            WeComMsg weComMsg = XmlUtil.xmlToBean(msg, WeComMsg.class);
            logger.info("handleWeComMsg weComMsg={}", weComMsg);
            // 回复文本消息
            WeComMsg returnMsg = new WeComMsg();
            returnMsg.setToUserName(weComMsg.getFromUserName());
            returnMsg.setFromUserName(weComMsg.getToUserName());
            returnMsg.setCreateTime(new Date().getTime());
            returnMsg.setMsgType(WeComMsgTypeEnum.RESP_MESSAGE_TYPE_TEXT.getValue());
            returnMsg.setFuncFlag(0);
            String returnMsgStr = XmlUtil.beanToXml(returnMsg, WeComMsg.class);
            result.setData(returnMsgStr);
            result.setCode(ResultData.OK);
        } catch (Exception e) {
            logger.error("xmlUtil.xmlToBean error", e);
        }
        return result;
    }

    /**
     * 全量同步企业微信组织架构
     * 依次同步部门、用户
     * @return 同步结果
     */
    @Override
    public ResultData<Void> syncWeComOrganization() {
        ResultData<Void> result = new ResultData<>();
        Long tenantId = SessionHolder.getCurrentTenantId();
        if (tenantId == null) {
            result.setErrorCode(ErrorCodeEnum.USER_NOT_JOIN_NORMAL_TENANT);
            return result;
        }
        ResultData<Map<String, String>> mapResult = tenantConfigService.queryTenantConfigMap(tenantId);
        Map<String, String> configMap = mapResult.getData();
        String corpId = configMap.get(TenantConfigEnum.WECOM_CORP_ID.getKey());
        String corpSecret = configMap.get(TenantConfigEnum.WECOM_CORP_SECRET.getKey());
        result = this.syncDepartment(corpId, corpSecret, tenantId);
        BaseAssert.assertTrue(result);
        result = this.syncUser(corpId, corpSecret, tenantId);
        BaseAssert.assertTrue(result);
        return result;
    }

    /**
     * 同步企业微信部门列表
     * @param tenantId 租户id
     * @return 同步结果
     */
    private ResultData<Void> syncDepartment(String corpId, String corpSecret, Long tenantId) {
        ResultData<Void> result = new ResultData<>();
        WeComDeptListRes deptResponse = weComUtil.getDepartmentList(null, corpId, corpSecret);
        if (deptResponse == null || CollectionUtil.isEmpty(deptResponse.getDepartment())) {
            logger.warn("syncDepartment wecom department list is empty, tenantId={}", tenantId);
            result.setCode(ResultData.OK);
            return result;
        }
        Map<Long, Long> wecomToDeptIdMap = new HashMap<>();
        List<DepartmentResult> deptList = departmentDao.queryWecomDeptList(tenantId);
        if (CollectionUtil.isNotEmpty(deptList)) {
            wecomToDeptIdMap = deptList.stream().collect(Collectors.toMap(DepartmentResult::getWecomId, DepartmentResult::getId, (a, b) -> a));
        }
        List<WeComDeptListRes.WeComDept> weComDeptList = deptResponse.getDepartment();
        logger.info("syncDepartment fetched departments, tenantId={}, count={}", tenantId, weComDeptList.size());
        // 按父部门id升序处理，确保父部门先于子部门创建
        weComDeptList.sort(Comparator.comparing(WeComDeptListRes.WeComDept::getParentid, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(WeComDeptListRes.WeComDept::getId));
        int createCount = 0;
        int updateCount = 0;
        for (WeComDeptListRes.WeComDept weComDept : weComDeptList) {
            DepartmentVO departmentVO = new DepartmentVO();
            departmentVO.setName(weComDept.getName());
            departmentVO.setOrderNum(weComDept.getOrder());
            // 父部门id映射为本地id，无映射时挂到根节点
            Long localPrtId = wecomToDeptIdMap.get(weComDept.getParentid());
            departmentVO.setPrtId(localPrtId == null ? 0L : localPrtId);
            Long localId = wecomToDeptIdMap.get(weComDept.getId());
            if (localId != null) {
                departmentVO.setId(localId);
                BaseAssert.assertTrue(departmentService.updateDepartment(departmentVO));
                updateCount++;
            } else {
                departmentVO.setWecomId(weComDept.getId());
                ResultData<Void> createResult = departmentService.createDepartment(departmentVO);
                BaseAssert.assertTrue(createResult);
                wecomToDeptIdMap.put(weComDept.getId(), createResult.getObjId());
                createCount++;
            }
        }
        logger.info("syncDepartment departments done, tenantId={}, created={}, updated={}", tenantId, createCount, updateCount);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 同步企业微信用户列表
     * @param tenantId 租户id
     * @return 同步结果
     */
    private ResultData<Void> syncUser(String corpId, String corpSecret, Long tenantId) {
        ResultData<Void> result = new ResultData<>();
        List<DepartmentResult> deptList = departmentDao.queryWecomDeptList(tenantId);
        if (deptList.isEmpty()) {
            logger.warn("syncUser no synced wecom departments found, tenantId={}", tenantId);
            result.setCode(ResultData.OK);
            return result;
        }
        Map<String, Long> wecomToUserIdMap = new HashMap<>();
        List<UserProfile> userList = userProfileDao.queryWecomProfileList(tenantId);
        if (CollectionUtil.isNotEmpty(userList)) {
            wecomToUserIdMap = userList.stream().collect(Collectors.toMap(UserProfile::getWecomId, UserProfile::getId, (a, b) -> a));
        }
        int createCount = 0;
        int updateCount = 0;
        int failCount = 0;
        for (DepartmentResult dept : deptList) {
            WeComUserListRes userResponse = weComUtil.getUserList(dept.getWecomId(), corpId, corpSecret);
            if (userResponse == null) {
                logger.warn("syncUser getUserList response is null, wecomDeptId={}", dept.getWecomId());
                continue;
            }
            if (userResponse.getErrcode() != null && userResponse.getErrcode() != 0) {
                logger.error("syncUser getUserList error, wecomDeptId={}, errcode={}, errmsg={}", dept.getWecomId(), userResponse.getErrcode(), userResponse.getErrmsg());
                result.setCode(ErrorCodeEnum.SYSTEM_ERROR.getValue());
                result.setMessage("获取企业微信用户失败: " + userResponse.getErrmsg());
                return result;
            }
            if (CollectionUtil.isEmpty(userResponse.getUserlist())) {
                continue;
            }
            for (WeComUserListRes.WeComUser weComUser : userResponse.getUserlist()) {
                try {
                    Long localUserId = wecomToUserIdMap.get(weComUser.getUserid());
                    UserVO userVO = this.buildSyncUserVO(weComUser, dept.getId(), localUserId);
                    if (localUserId != null) {
                        BaseAssert.assertTrue(userService.updateUser(userVO));
                        updateCount++;
                    } else {
                        ResultData<Long> createResult = userService.createUser(userVO);
                        BaseAssert.assertTrue(createResult);
                        wecomToUserIdMap.put(weComUser.getUserid(), createResult.getData());
                        createCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    logger.warn("syncUser user fail, wecomUserId={}, error={}", weComUser.getUserid(), e.getMessage());
                }
            }
        }
        logger.info("syncUser users done, tenantId={}, created={}, updated={}, failed={}", tenantId, createCount, updateCount, failCount);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 构建同步用户信息
     * @param weComUser 企微用户
     * @param deptId 本地部门id
     * @param localUserId 本地用户id，为空表示新用户
     * @return 用户信息
     */
    private UserVO buildSyncUserVO(WeComUserListRes.WeComUser weComUser, Long deptId, Long localUserId) {
        UserVO userVO = new UserVO();
        userVO.setId(localUserId);
        userVO.setName(weComUser.getName());
        userVO.setDeptId(deptId);
        userVO.setPhone(weComUser.getMobile());
        userVO.setEmail(weComUser.getEmail() != null ? weComUser.getEmail() : weComUser.getBizMail());
        if (weComUser.getGender() != null && weComUser.getGender() > 0) {
            userVO.setSex(weComUser.getGender());
        }
        if (localUserId == null) {
            userVO.setWecomId(weComUser.getUserid());
            userVO.setLoginName(weComUser.getUserid());
        }
        return userVO;
    }
}
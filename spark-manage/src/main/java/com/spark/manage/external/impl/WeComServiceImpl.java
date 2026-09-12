package com.spark.manage.external.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.UserProfile;
import com.spark.common.bean.sys.query.DepartmentQuery;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            logger.error("XmlUtil.xmlToBean error", e);
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
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        ResultData<Map<String, String>> mapResult = tenantConfigService.queryTenantConfigMap(tenantId);
        Map<String, String> configMap = mapResult.getData();
        String corpId = configMap.get(TenantConfigEnum.WECOM_CORP_ID.getKey());
        String corpSecret = configMap.get(TenantConfigEnum.WECOM_CORP_SECRET.getKey());
        result = this.syncDepartment(corpId, corpSecret);
        BaseAssert.assertTrue(result);
        result = this.syncUser(corpId, corpSecret);
        BaseAssert.assertTrue(result);
        return result;
    }

    /**
     * 同步企业微信部门列表
     * @return 同步结果
     */
    private ResultData<Void> syncDepartment(String corpId, String corpSecret) {
        ResultData<Void> result = new ResultData<>();
        try {
            WeComDeptListRes deptResponse = weComUtil.getDepartmentList(null, corpId, corpSecret);
            logger.info("syncDepartmentList deptResponse={}", deptResponse);
            if (deptResponse == null || CollectionUtil.isEmpty(deptResponse.getDepartment())) {
                logger.warn("syncDepartment WeCom department list is empty");
                result.setCode(ResultData.OK);
                return result;
            }
            List<WeComDeptListRes.WeComDept> weComDeptList = deptResponse.getDepartment();
            logger.info("syncDepartment fetched {} departments", weComDeptList.size());
            // 按parentid排序，确保父部门先处理
            weComDeptList.sort((a, b) -> {
                int cmp = a.getParentid().compareTo(b.getParentid());
                if (cmp == 0) {
                    return a.getId().compareTo(b.getId());
                }
                return cmp;
            });
            // 构建wecomId到本地id的映射
            Map<Long, Long> wecomToLocalIdMap = new HashMap<>();
            int createCount = 0;
            int updateCount = 0;
            for (WeComDeptListRes.WeComDept weComDept : weComDeptList) {
                DepartmentResult existDept = departmentDao.queryByWecomId(weComDept.getId());
                DepartmentVO departmentVO = new DepartmentVO();
                departmentVO.setName(weComDept.getName());
                departmentVO.setOrderNum(weComDept.getOrder());
                // 映射父部门id到本地id
                if (weComDept.getParentid() != null && weComDept.getParentid() > 0) {
                    Long localPrtId = wecomToLocalIdMap.get(weComDept.getParentid());
                    if (localPrtId != null) {
                        departmentVO.setPrtId(localPrtId);
                    }
                } else {
                    departmentVO.setPrtId(0L);
                }
                if (existDept != null) {
                    // 更新已有部门
                    departmentVO.setId(existDept.getId());
                    departmentService.updateDepartment(departmentVO);
                    wecomToLocalIdMap.put(weComDept.getId(), existDept.getId());
                    updateCount++;
                } else {
                    // 创建新部门
                    departmentVO.setWecomId(weComDept.getId());
                    departmentService.createDepartment(departmentVO);
                    DepartmentResult newDept = departmentDao.queryByWecomId(weComDept.getId());
                    if (newDept != null) {
                        wecomToLocalIdMap.put(weComDept.getId(), newDept.getId());
                        createCount++;
                    }
                }
            }
            logger.info("syncDepartment created {} departments, updated {} departments", createCount, updateCount);
            result.setCode(ResultData.OK);
        } catch (Exception e) {
            logger.error("syncDepartment error", e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
        }
        return result;
    }

    /**
     * 同步企业微信用户列表
     * @return 同步结果
     */
    private ResultData<Void> syncUser(String corpId, String corpSecret) {
        ResultData<Void> result = new ResultData<>();
        try {
            // 获取所有已同步的部门
            DepartmentQuery deptQuery = new DepartmentQuery();
            deptQuery.setPage(false);
            List<DepartmentResult> deptList = departmentDao.queryDepartmentList(deptQuery);
            if (deptList == null || deptList.isEmpty()) {
                logger.warn("syncUser local department list is empty, please sync department first");
                result.setCode(ResultData.OK);
                return result;
            }
            // 过滤出有wecomId的部门
            List<DepartmentResult> syncedDeptList = deptList.stream().filter(d -> d.getWecomId() != null).toList();
            if (syncedDeptList.isEmpty()) {
                logger.warn("syncUser no synced WeCom departments found");
                result.setCode(ResultData.OK);
                return result;
            }
            int createCount = 0;
            int updateCount = 0;
            for (DepartmentResult dept : syncedDeptList) {
                WeComUserListRes userResponse = weComUtil.getUserList(dept.getWecomId(), corpId, corpSecret);
                if (userResponse == null || userResponse.getUserlist() == null) {
                    continue;
                }
                List<WeComUserListRes.WeComUser> weComUsers = userResponse.getUserlist();
                for (WeComUserListRes.WeComUser weComUser : weComUsers) {
                    UserProfile existProfile = userProfileDao.queryByWecomId(weComUser.getUserid());
                    UserVO userVO = new UserVO();
                    userVO.setName(weComUser.getName());
                    userVO.setDeptId(dept.getId());
                    userVO.setPhone(weComUser.getMobile());
                    userVO.setEmail(weComUser.getEmail() != null ? weComUser.getEmail() : weComUser.getBizMail());
                    if (weComUser.getGender() != null && weComUser.getGender() > 0) {
                        userVO.setSex(weComUser.getGender());
                    }
                    if (existProfile != null) {
                        // 更新已有用户
                        userVO.setId(existProfile.getId());
                        userService.updateUser(userVO);
                        updateCount++;
                    } else {
                        // 创建新用户
                        userVO.setWecomId(weComUser.getUserid());
                        userVO.setLoginName(weComUser.getUserid());
                        ResultData<Long> createResult = userService.createUser(userVO);
                        if (createResult.getCode() == ResultData.OK) {
                            createCount++;
                        }
                    }
                }
            }
            logger.info("syncUser created ={} users, updated ={} users", createCount, updateCount);
            result.setCode(ResultData.OK);
        } catch (Exception e) {
            logger.error("syncUser error", e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
        }
        return result;
    }
}
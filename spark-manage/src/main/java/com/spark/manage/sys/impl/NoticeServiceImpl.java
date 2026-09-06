package com.spark.manage.sys.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.Notice;
import com.spark.common.bean.sys.query.DepartmentQuery;
import com.spark.common.bean.sys.query.NoticeObjQuery;
import com.spark.common.bean.sys.query.NoticeQuery;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.result.NoticeObjResult;
import com.spark.common.bean.sys.result.NoticeResult;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.NoticeVO;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.sys.DepartmentDao;
import com.spark.dao.sys.NoticeDao;
import com.spark.dao.sys.NoticeObjDao;
import com.spark.dao.sys.UserDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.NoticeStatusEnum;
import com.spark.common.enums.NoticeTypeEnum;
import com.spark.manage.BaseService;
import com.spark.manage.sys.INoticeService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.common.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
 * @since 2024/5/6 13:36
 */
@Service
@LogPrint
public class NoticeServiceImpl extends BaseService<NoticeQuery, NoticeResult> implements INoticeService {
    private final static Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);
    @Autowired
    private NoticeDao noticeDao;
    @Autowired
    private NoticeObjDao noticeObjDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Value("${notice.file.path}")
    private String noticeFilePath;

    /**
     * 创建公告
     * @param noticeVO 公告数据
     * @return 创建结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.NOTICE_INSERT)
    public ResultData<Void> createNotice(NoticeVO noticeVO) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (noticeVO == null || StringUtil.isBlank(noticeVO.getTitle()) || noticeVO.getType() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Notice notice = new Notice();
        BeanUtil.copyProperties(noticeVO, notice);
        int count = noticeDao.insertDB(notice);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        Long noticeId = notice.getId();
        List<Long> objIds = noticeVO.getObjIds();
        if (CollectionUtil.isNotEmpty(objIds)) {
            count = noticeObjDao.batchInsert(noticeId, objIds, userId);
            if (count < 1) {
                result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
                return result;
            }
        }
        result.setObjId(noticeId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改公告
     *
     * @param noticeVO 修改的参数
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.NOTICE_UPDATE)
    public ResultData<Void> updateNotice(NoticeVO noticeVO) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (noticeVO == null || noticeVO.getId() == null || noticeVO.getUpdateObjIds() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        NoticeQuery query = new NoticeQuery();
        query.setId(noticeVO.getId());
        NoticeResult noticeResult = noticeDao.queryNotice(query);
        if (noticeResult == null) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_EXIST);
            return result;
        }
        if (NoticeStatusEnum.RELEASED.getValue().equals(noticeResult.getStatus())) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_ALLOW);
            return result;
        }
        Notice notice = new Notice();
        BeanUtil.copyProperties(noticeVO, notice);
        int count = noticeDao.updateDBById(notice);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        if (noticeVO.getUpdateObjIds()) {
            count = noticeObjDao.deleteByNoticeId(noticeVO.getId(), userId);
            if (count < 0) {
                result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
                return result;
            }
            List<Long> objIds = noticeVO.getObjIds();
            if (CollectionUtil.isNotEmpty(objIds)) {
                objIds = objIds.stream().distinct().collect(Collectors.toList());
                count = noticeObjDao.batchInsert(notice.getId(), objIds, userId);
                if (count < 1) {
                    result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
                    return result;
                }
            }
        }
        result.setObjId(noticeVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 下架公告
     *
     * @param noticeVO 下架公告参数
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.NOTICE_DELIST)
    public ResultData<Void> delistNotice(NoticeVO noticeVO) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (noticeVO == null || noticeVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        NoticeQuery query = new NoticeQuery();
        query.setId(noticeVO.getId());
        NoticeResult noticeResult = noticeDao.queryNotice(query);
        if (noticeResult == null) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_EXIST);
            return result;
        }
        if (!NoticeStatusEnum.RELEASED.getValue().equals(noticeResult.getStatus())) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_DELIST_ALLOW);
            return result;
        }
        Notice notice = new Notice();
        BeanUtil.copyProperties(noticeVO, notice);
        notice.setStatus(NoticeStatusEnum.DELISTED.getValue());
        int count = noticeDao.updateDBById(notice);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(noticeVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除公告
     *
     * @param noticeVO 删除的参数
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.NOTICE_DELETE)
    public ResultData<Void> deleteNotice(NoticeVO noticeVO) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (noticeVO == null || noticeVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        NoticeQuery query = new NoticeQuery();
        query.setId(noticeVO.getId());
        NoticeResult noticeResult = noticeDao.queryNotice(query);
        if (noticeResult == null) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_EXIST);
            return result;
        }
        if (NoticeStatusEnum.RELEASED.getValue().equals(noticeResult.getStatus())) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_ALLOW);
            return result;
        }
        Notice notice = new Notice();
        notice.setId(noticeVO.getId());
        int count = noticeDao.deleteDBById(notice);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        count = noticeObjDao.deleteByNoticeId(notice.getId(), userId);
        if (count < 0) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(noticeVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查看公告详情
     *
     * @param query 查看公告详情参数
     * @return 查询结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.NOTICE_DETAIL)
    public ResultData<NoticeResult> detailNotice(NoticeQuery query) {
        ResultData<NoticeResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        NoticeResult noticeResult = noticeDao.queryNotice(query);
        if (noticeResult == null) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_EXIST);
            return result;
        }
        NoticeObjQuery noticePubQuery = new NoticeObjQuery();
        noticePubQuery.setNoticeId(query.getId());
        List<NoticeObjResult> objList = noticeObjDao.queryNoticeObjList(noticePubQuery);
        if (CollectionUtil.isNotEmpty(objList)) {
            List<Long> objIds = objList.stream().map(NoticeObjResult::getObjId).collect(Collectors.toList());
            List<String> objNames = super.getObjNames(objIds);
            noticeResult.setObjIds(objIds);
            noticeResult.setObjNames(objNames);
        }
        result.setData(noticeResult);
        result.setObjId(query.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询公告
     *
     * @param query 查询参数
     * @return 查询结果
     */
    @Override
    public ResultData<PageResult<NoticeResult>> pageNoticeList(NoticeQuery query) {
        ResultData<PageResult<NoticeResult>> result = new ResultData<>();
        if (query == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        PageResult<NoticeResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 保存公告文本
     *
     * @param noticeVO 保存公告文本参数
     * @return 保存结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.NOTICE_SAVE_TEXT)
    public ResultData<Void> saveNoticeText(NoticeVO noticeVO) {
        ResultData<Void> result = new ResultData<>();
        if (noticeVO == null || noticeVO.getId() == null || StringUtil.isBlank(noticeVO.getContent())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        NoticeQuery query = new NoticeQuery();
        query.setId(noticeVO.getId());
        NoticeResult noticeResult = noticeDao.queryNotice(query);
        if (noticeResult == null) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_EXIST);
            return result;
        }
        if (NoticeStatusEnum.RELEASED.getValue().equals(noticeResult.getStatus())) {
            result.setErrorCode(ErrorCodeEnum.NOTICE_NOT_ALLOW);
            return result;
        }
        String filePath = noticeFilePath + noticeVO.getId() + ".txt";
        File file = new File(filePath);
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                logger.error("file delete fail!");
                return result;
            }
        }
        result = TextUtil.writeToText(filePath, noticeVO.getContent());
        result.setObjId(noticeVO.getId());
        return result;
    }

    /**
     * 查询公告列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<List<NoticeResult>> queryNoticeList(NoticeQuery query) {
        ResultData<List<NoticeResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        List<Long> objIds = new ArrayList<>();
        objIds.add(userId);
        UserQuery userQuery = new UserQuery();
        userQuery.setId(userId);
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult == null) {
            result.setErrorCode(ErrorCodeEnum.USER_NOT_EXIST);
            return result;
        }
        Long deptId = userResult.getDeptId();
        if (deptId != null) {
            DepartmentQuery departmentQuery = new DepartmentQuery();
            departmentQuery.setId(deptId);
            DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
            if (departmentResult == null) {
                result.setErrorCode(ErrorCodeEnum.DEPT_NOT_EXIST);
                return result;
            }
            String code = departmentResult.getCode();
            List<String> codeList = parsePrtCodes(code);
            departmentQuery.setId(null);
            departmentQuery.setCodes(codeList);
            departmentQuery.setPage(false);
            List<DepartmentResult> departmentList = departmentDao.queryDepartmentList(departmentQuery);
            if (CollectionUtil.isNotEmpty(departmentList)) {
                List<Long> deptIds = departmentList.stream().map(DepartmentResult::getId).toList();
                objIds.addAll(deptIds);
            }
        }
        NoticeObjQuery noticeObjQuery = new NoticeObjQuery();
        noticeObjQuery.setObjIds(objIds);
        noticeObjQuery.setPage(false);
        List<NoticeObjResult> noticeObjList = noticeObjDao.queryNoticeObjList(noticeObjQuery);
        if (CollectionUtil.isEmpty(noticeObjList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<Long> noticeIds = noticeObjList.stream().map(NoticeObjResult::getNoticeId).distinct().collect(Collectors.toList());
        query.setIds(noticeIds);
        query.setPage(false);
        query.setStatus(NoticeStatusEnum.RELEASED.getValue());
        List<NoticeResult> noticeList = noticeDao.queryNoticeList(query);
        this.supplyList(noticeList);
        result.setData(noticeList);
        result.setObjId(query.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查看公告文本
     * @param query 查询参数
     * @return  文本
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.NOTICE_VIEW_TEXT)
    public ResultData<String> viewNoticeText(NoticeQuery query) {
        ResultData<String> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String filePath = noticeFilePath + query.getId() + ".txt";
        File file = new File(filePath);
        if (!file.exists()) {
            result.setCode(ResultData.OK);
            return result;
        }
        result = TextUtil.getFromText(filePath, true);
        return result;
    }

    /**
     * 解析所有父的code
     * @param code 部门code
     * @return 所有父的code
     */
    public static List<String> parsePrtCodes(String code) {
        List<String> codeList = new ArrayList<>();
        if (StringUtil.isBlank(code)) {
            return codeList;
        }
        while (code.length() >= 3) {
            codeList.add(code);
            code = code.substring(0,code.length()-3);
        }
        return codeList;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<NoticeResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        List<Long> noticeIds = list.stream().map(NoticeResult::getId).toList();
        NoticeObjQuery noticePubQuery = new NoticeObjQuery();
        noticePubQuery.setNoticeIds(noticeIds);
        List<NoticeObjResult> objList = noticeObjDao.queryNoticeObjList(noticePubQuery);
        Map<Long,List<NoticeObjResult>> objMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(objList)) {
            objMap = objList.stream().collect(Collectors.groupingBy(NoticeObjResult::getNoticeId));
        }
        Map<Long, List<NoticeObjResult>> finalObjMap = objMap;
        list.forEach(item -> {
            NoticeTypeEnum noticeTypeEnum = NoticeTypeEnum.indexOf(item.getType());
            item.setTypeName(noticeTypeEnum.getName());
            NoticeStatusEnum noticeStatusEnum = NoticeStatusEnum.indexOf(item.getStatus());
            item.setStatusName(noticeStatusEnum.getName());
            if (!finalObjMap.containsKey(item.getId())) {
                return;
            }
            List<NoticeObjResult> noticeObjList = finalObjMap.get(item.getId());
            List<Long> objIds = noticeObjList.stream().map(NoticeObjResult::getObjId).distinct().toList();
            List<String> objNames = super.getObjNames(objIds);
            item.setObjIds(objIds);
            item.setObjNames(objNames);
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(NoticeQuery query) {
        return noticeDao.queryNoticeCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<NoticeResult> queryList(NoticeQuery query) {
        return noticeDao.queryNoticeList(query);
    }
}

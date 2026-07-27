package com.spark.dao.system;

import com.spark.bean.system.query.NoticeObjQuery;
import com.spark.bean.system.result.NoticeObjResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/7 12:41
 */
public interface NoticeObjDao {
    /**
     * 批量插入
     * @param noticeId
     * @param objIds
     * @param createdBy
     * @return
     */
    int batchInsert(@Param("noticeId") Long noticeId, @Param("objIds") List<Long> objIds,@Param("createdBy") Long createdBy);

    /**
     * 根据通知id删除
     * @param noticeId
     * @param updatedBy
     * @return
     */
    int deleteByNoticeId(@Param("noticeId") Long noticeId ,@Param("updatedBy") Long updatedBy);

    /**
     * 查询列表
     * @param noticePubQuery
     * @return
     */
    List<NoticeObjResult> queryNoticeObjList(NoticeObjQuery noticePubQuery);
}

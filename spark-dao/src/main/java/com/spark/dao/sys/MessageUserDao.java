package com.spark.dao.sys;

import com.spark.common.bean.sys.query.MessageUserQuery;
import com.spark.common.bean.sys.result.MessageUserResult;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/18 17:45
 */
public interface MessageUserDao {

    /**
     * 批量插入
     * @param id
     * @param userIds
     * @param userId
     * @return
     */
    int batchInsert(@Param("msgId") Long id, @Param("userIds") List<Long> userIds,@Param("createdBy") Long userId);

    /**
     * 查询列表
     * @param messageUserQuery
     * @return
     */
    List<MessageUserResult> queryMessageUserList(MessageUserQuery messageUserQuery);

    /**
     * 按消息id逻辑删除
     * @param msgId 消息id
     * @param updatedBy 修改人
     * @param updatedDt 修改时间
     * @return 删除数量
     */
    int deleteByMsgId(@Param("msgId") Long msgId, @Param("updatedBy") Long updatedBy, @Param("updatedDt") Date updatedDt);
}

package com.spark.dao.system;

import com.spark.bean.system.query.MessageUserQuery;
import com.spark.bean.system.result.MessageUserResult;
import org.apache.ibatis.annotations.Param;

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
}

package com.spark.bean.chat.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:18
 */
@Data
public class ChatSpaceQuery extends BaseQuery {
    /**
     * 聊天空间id
     */
    private Long spaceId;

    /**
     * 发送人id
     */
    private Long senderId;

    /**
     * 接收人id
     */
    private Long receiverId;
}

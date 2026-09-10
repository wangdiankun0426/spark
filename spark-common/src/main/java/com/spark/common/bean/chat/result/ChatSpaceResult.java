package com.spark.common.bean.chat.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:19
 */
@Data
public class ChatSpaceResult extends BaseResult {

    /**
     * 空间id
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

    /**
     * 会话类型
     */
    private Integer spaceType;

    /**
     * 会话标题
     */
    private String title;
}

package com.spark.bean.chat.result;

import com.spark.bean.system.result.UserResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/18 22:26
 */
@Data
public class ChatUserResult extends UserResult {

    /**
     * 聊天空间id
     */
    private Long chatSpaceId;

    /**
     * 未读消息数量
     */
    private Integer noReadCount = 0;
}

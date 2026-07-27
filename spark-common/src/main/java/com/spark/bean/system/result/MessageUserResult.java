package com.spark.bean.system.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/21 22:46
 */
@Data
public class MessageUserResult extends BaseResult {
    /**
     * 消息id
     */
    private Long msgId;

    /**
     * 用户id
     */
    private Long userId;
}

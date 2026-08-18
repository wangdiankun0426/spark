package com.spark.bean.system.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/21 22:42
 */
@Data
public class MessageUserQuery extends BaseQuery {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 消息id
     */
    private Long msgId;

    /**
     * 消息ids
     */
    private List<Long> msgIds;
}

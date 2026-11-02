package com.spark.common.bean.sys.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-02 12:56:03
 * 我的消息分类计数：消息页筛选标签右上角的数量，一次请求取回全部、未读、已读三个数，避免多次 count
 */
@Data
public class MessageCountResult {

    /**
     * 全部消息数
     */
    private Integer total;

    /**
     * 未读消息数
     */
    private Integer unread;

    /**
     * 已读消息数
     */
    private Integer readCount;
}

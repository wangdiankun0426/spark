package com.spark.common.bean.sys.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-06-18 16:08:03
 */
@Data
public class MessageQuery extends BaseQuery {

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收人ID：查询「我的消息」时按 sys_message_user 关联过滤
     */
    private Long userId;

    /**
     * 列表过滤器：all-全部 unread-未读 read-已读
     * 由服务端翻译成 readFlag 条件，前端不再自行过滤分页数据
     */
    private String filterType;

    /**
     * 阅读状态：0-未读 1-已读
     */
    private Integer readFlag;

}

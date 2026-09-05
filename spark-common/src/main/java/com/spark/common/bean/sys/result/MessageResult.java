package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

import java.util.List;

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
public class MessageResult extends BaseResult {

    /**
     * 消息类型
     **/
    private Integer type;

    /**
     * 消息标题
     **/
    private String title;

    /**
     * 消息内容
     **/
    private String content;

    /**
     * 所属对象id
     */
    private Long refId;

    /**
     * 消息类型名称
     */
    private String typeName;

    /**
     * 接收人ids
     */
    private List<Long> userIds;

    /**
     * 接收人姓名
     */
    private List<String> userNames;
}

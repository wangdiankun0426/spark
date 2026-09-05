package com.spark.common.bean.sys.entity;

import com.spark.common.bean.base.BaseEntity;
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
public class Message extends BaseEntity {

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
}

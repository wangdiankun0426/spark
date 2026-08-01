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
}

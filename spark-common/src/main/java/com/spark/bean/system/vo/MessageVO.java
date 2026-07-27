package com.spark.bean.system.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-06-18 15:44:39
 */
@Data
public class MessageVO extends BaseVO {

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
     * 接收人ids
     */
    private List<Long> userIds;

}

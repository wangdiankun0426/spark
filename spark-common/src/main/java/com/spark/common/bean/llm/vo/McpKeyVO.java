package com.spark.common.bean.llm.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-15 14:00:00
 */
@Data
public class McpKeyVO extends BaseVO {

    /**
     * 密钥名称
     **/
    private String name;

    /**
     * 绑定用户id
     **/
    private Long userId;

    /**
     * 过期时间，格式yyyy-MM-dd HH:mm:ss
     **/
    private String expireDt;

    /**
     * 状态：1:启用，-1:禁用
     **/
    private Integer status;

    /**
     * 描述
     **/
    private String description;

}

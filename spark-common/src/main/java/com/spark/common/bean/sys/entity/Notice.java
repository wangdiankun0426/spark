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
 * @since 2024/5/6 13:32
 */
@Data
public class Notice extends BaseEntity {
    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告类型
     */
    private Integer type;

    /**
     * 公告状态
     */
    private Integer status;
}

package com.spark.bean.system.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/7 14:05
 */
@Data
public class NoticeQuery extends BaseQuery {

    /**
     * 公告类型
     */
    private Integer type;

    /**
     * 公告状态
     */
    private Integer status;
}

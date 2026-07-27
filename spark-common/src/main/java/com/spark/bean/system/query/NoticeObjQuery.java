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
 * @since 2024/5/7 14:13
 */
@Data
public class NoticeObjQuery extends BaseQuery {
    /**
     * 公告id
     */
    private Long noticeId;

    /**
     * 对象ids
     */
    private List<Long> objIds;

    /**
     * 公告ids
     */
    private List<Long> noticeIds;

}

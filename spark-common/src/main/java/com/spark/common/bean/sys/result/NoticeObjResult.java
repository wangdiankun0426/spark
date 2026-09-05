package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/7 14:14
 */
@Data
public class NoticeObjResult extends BaseResult {
    /**
     * 公告id
     */
    private Long noticeId;

    /**
     * 授权对象
     */
    private Long objId;
}

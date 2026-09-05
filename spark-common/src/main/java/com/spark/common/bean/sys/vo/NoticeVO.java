package com.spark.common.bean.sys.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/6 13:41
 */
@Data
public class NoticeVO extends BaseVO {
    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型
     */
    private Integer type;

    /**
     * 公告状态
     */
    private Integer status;

    /**
     * 授权对象
     */
    private List<Long> objIds;

    /**
     * 更新授权对象
     */
    private Boolean updateObjIds;
}

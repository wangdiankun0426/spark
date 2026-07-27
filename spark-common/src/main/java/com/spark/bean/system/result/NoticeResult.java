package com.spark.bean.system.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/6 13:50
 */
@Data
public class NoticeResult extends BaseResult {
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
     * 公告类型名称
     */
    private String typeName;

    /**
     * 公告状态
     */
    private Integer status;

    /**
     * 公告状态名称
     */
    private String statusName;

    /**
     * 接受对象
     */
    private List<Long> objIds;

    /**
     * 接受对象名称
     */
    private List<String> objNames;
}

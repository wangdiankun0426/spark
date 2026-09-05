package com.spark.common.bean.form.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/3 13:28
 */
@Data
public class FormVersion extends BaseEntity {

    /**
     * 表单id
     */
    private Long formId;

    /**
     * 当前版本值
     */
    private Integer revCode;

    /**
     * 当前版本号
     */
    private String revNum;

    /**
     * 文件路径
     **/
    private String filePath;
}

package com.spark.common.bean.form.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/3 14:12
 */
@Data
public class FormVersionResult extends BaseResult {
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

    /**
     * 排序号
     **/
    private Integer orderNum;

}

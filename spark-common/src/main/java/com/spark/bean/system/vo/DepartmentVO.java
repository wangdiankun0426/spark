package com.spark.bean.system.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 18:48
 */
@Data
public class DepartmentVO extends BaseVO {

    /**
     * 名称
     */
    private String name;

    /**
     * 父级id
     */
    private Long prtId;

    /**
     * 部门主管id
     */
    private Long headerId;

    /**
     * 部门编号
     */
    private String deptNum;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer orderNum;
}

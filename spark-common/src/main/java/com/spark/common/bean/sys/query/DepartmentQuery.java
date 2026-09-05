package com.spark.common.bean.sys.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 20:08
 */
@Data
public class DepartmentQuery extends BaseQuery {

    /**
     * 父id
     */
    private Long prtId;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门层级码
     */
    private String code;

    /**
     * 部门层级码数组
     */
    private List<String> codes;

    /**
     * 部门编号
     */
    private String deptNum;

    /**
     * 状态
     */
    private Integer status;
}

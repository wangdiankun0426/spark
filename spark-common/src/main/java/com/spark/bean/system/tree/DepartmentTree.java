package com.spark.bean.system.tree;

import com.spark.bean.base.BaseTree;
import com.spark.enums.StatusEnum;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/19 19:58
 */
@Data
public class DepartmentTree extends BaseTree<DepartmentTree> {

    /**
     * 名称
     */
    private String name;

    /**
     * 层级码
     */
    private String code;

    /**
     * 部门主管id
     */
    private Long headerId;

    /**
     * 部门主管名称
     */
    private String headerName;

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

    /**
     * 状态名称
     */
    private String statusName;

    public String getStatusName() {
        if (this.status == null) {
            return null;
        }
        return StatusEnum.indexOf(this.status).getDesc();
    }

}

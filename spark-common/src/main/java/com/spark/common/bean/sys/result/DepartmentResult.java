package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import com.spark.common.enums.StatusEnum;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 19:45
 */
@Data
public class DepartmentResult extends BaseResult {
    /**
     * 父级id
     */
    private Long prtId;

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
     * 企业微信部门ID
     */
    private Long wecomId;

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

package com.spark.bean.system.result;

import com.spark.bean.base.BaseResult;
import com.spark.enums.StatusEnum;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/25 21:41
 */
@Data
public class RoleResult extends BaseResult {

    /**
     * 名称
     */
    private String name;

    /**
     * 数据权限
     */
    private Integer dataScope;

    /**
     * 数据权限
     */
    private String dataScopeName;

    /**
     * 状态 StatusEnum
     */
    private Integer status;

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

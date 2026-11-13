package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import com.spark.common.enums.StatusEnum;
import lombok.Data;

import java.util.List;

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
     * 菜单父子联动 1联动0独立
     */
    private Integer menuCheckStrictly;

    /**
     * 菜单id列表，角色详情返回授权勾选
     */
    private List<Long> menuIds;

    /**
     * 部门id列表，角色详情返回自定义数据范围勾选
     */
    private List<Long> deptIds;

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

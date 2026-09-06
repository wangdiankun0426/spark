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
 * @since 2024/2/25 21:34
 */
@Data
public class RoleVO extends BaseVO {

    /**
     * 名称
     */
    private String name;

    /**
     * 数据权限 DataScopeEnum
     */
    private Integer dataScope;

    /**
     * 状态 StatusEnum
     */
    private Integer status;

    /**
     * 菜单ID列表
     */
    private List<Long> menuIds;
}

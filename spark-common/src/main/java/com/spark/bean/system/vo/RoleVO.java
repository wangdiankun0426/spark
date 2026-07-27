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
}

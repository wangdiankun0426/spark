package com.spark.bean.system.vo;

import com.spark.bean.base.BaseVO;
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
public class RoleUserVO extends BaseVO {
    /**
     * 用户id数组
     */
    private List<Long> userIds;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色id
     */
    private Long roleId;
}

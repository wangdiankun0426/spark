package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/27 20:17
 */
@Data
public class RoleUserResult extends BaseResult {
    /**
     *
     */
    private Long roleId;

    /**
     *
     */
    private Long  userId;

    /**
     *
     */
    private String userName;

    /**
     *
     */
    private String loginName;
}

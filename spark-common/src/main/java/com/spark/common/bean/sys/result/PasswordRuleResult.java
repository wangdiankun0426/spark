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
 * @since 2026-09-14 10:00:00
 * 密码长度规则
 */
@Data
public class PasswordRuleResult extends BaseResult {

    /**
     * 密码最小长度
     */
    private Integer minLength;

    /**
     * 密码最大长度
     */
    private Integer maxLength;
}

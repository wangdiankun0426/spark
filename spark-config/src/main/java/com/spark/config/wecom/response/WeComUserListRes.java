package com.spark.config.wecom.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 14:00:00
 * 企业微信用户列表响应
 */
@Data
public class WeComUserListRes {

    /**
     * 错误码（0表示成功）
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    /**
     * 用户列表
     */
    private List<WeComUser> userlist;

    /**
     * +++/\_/\
     * + ( °w° )=
     * +++)   (  //
     * + (__ __)//
     *
     * @author wangdiankun
     * @since 2026-07-28 14:00:00
     * 企业微信用户信息
     */
    @Data
    public static class WeComUser {

        /**
         * 用户id
         */
        private String userid;

        /**
         * 用户名称
         */
        private String name;

        /**
         * 别名
         */
        private String alias;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 性别（0未定义，1男，2女）
         */
        private Integer gender;

        /**
         * 头像url
         */
        private String avatar;

        /**
         * 所属部门id列表
         */
        private List<Long> department;

        /**
         * 主部门id
         */
        @JsonProperty("main_department")
        private Long mainDepartment;

        /**
         * 激活状态（1=已激活，2=已禁用）
         */
        private Integer status;

        /**
         * 企业邮箱
         */
        @JsonProperty("biz_mail")
        private String bizMail;

        /**
         * 座机
         */
        private String telephone;
    }
}
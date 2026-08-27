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
 * 企业微信部门列表响应
 */
@Data
public class WeComDeptListRes {

    /**
     * 错误码（0表示成功）
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    /**
     * 部门列表
     */
    private List<WeComDept> department;

    /**
     * +++/\_/\
     * + ( °w° )=
     * +++)   (  //
     * + (__ __)//
     *
     * @author wangdiankun
     * @since 2026-07-28 14:00:00
     * 企业微信部门信息
     */
    @Data
    public static class WeComDept {

        /**
         * 部门id
         */
        private Long id;

        /**
         * 部门名称
         */
        private String name;

        /**
         * 父部门id（根部门为0）
         */
        private Long parentid;

        /**
         * 排序值
         */
        private Integer order;

        /**
         * 部门负责人
         */
        @JsonProperty("department_leader")
        private List<String> departmentLeader;
    }
}
package com.spark.bean.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/24 22:37
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseTree<T> implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 父id
     */
    private Long prtId;

    /**
     * 子级
     */
    private List<T> children;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建人名称
     */
    private String createdByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp createdDt;

    /**
     * 修改人
     */
    private Long updatedBy;

    /**
     * 修改人名称
     */
    private String updatedByName;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp updatedDt;
}

package com.spark.bean.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/25 21:33
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseVO implements Serializable {
    /**
     * id
     */
    private Long id;
}

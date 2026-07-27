package com.spark.bean.kb.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-12-07 19:08:01
 */
@Data
public class DocumentVO extends BaseVO {

    /**
     * 父ID
     **/
    private Long prtId;

    /**
     * 文档归属类型
     **/
    private Integer documentType;

    /**
     * 名称
     **/
    private String name;

    /**
     * 大小
     **/
    private Long size;

    /**
     * 存储路径
     **/
    private String path;

    /**
     * 拓展名
     **/
    private String ext;

}

package com.spark.bean.kb.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/9 下午12:56
 */
@Data
public class DocumentSearchQuery extends BaseQuery {

    /**
     * 关键词
     */
    private String keyWord;

    /**
     * 检索类型 DocumentSearchTypeEnum
     */
    private String searchType;

    /**
     * 文档归属类型
     **/
    private Integer documentType;

    /**
     * 父ID
     **/
    private Long prtId;

}

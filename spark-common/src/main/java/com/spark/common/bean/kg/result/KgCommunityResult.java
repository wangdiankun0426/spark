package com.spark.common.bean.kg.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 18:00:00
 * 图谱社区查询结果
 */
@Data
public class KgCommunityResult extends BaseResult {

    /**
     * 图谱 id
     */
    private Long graphId;

    /**
     * 社区序号
     */
    private Integer communityIndex;

    /**
     * 社区名称
     */
    private String name;

    /**
     * 社区摘要
     */
    private String summary;

    /**
     * 成员实体数量
     */
    private Integer memberCount;

}

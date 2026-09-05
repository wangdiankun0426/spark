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
 * @since 2026-07-23 14:00:00
 * 知识图谱实体数量统计结果
 */
@Data
public class KgEntityCountResult extends BaseResult {

    /**
     * 图谱 id
     */
    private Long graphId;

    /**
     * 实体数量
     */
    private Integer entityCount;

}

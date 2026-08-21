package com.spark.bean.task.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-21 15:30:00
 * 任务产出数据表
 */
@Data
public class TaskInstanceData extends BaseEntity {

    /**
     * 任务组id
     */
    private String setId;

    /**
     * 任务实例id
     */
    private Long taskId;

    /**
     * 数据编码
     * TaskParamCode
     */
    private String code;

    /**
     * 数据值
     */
    private String value;

}

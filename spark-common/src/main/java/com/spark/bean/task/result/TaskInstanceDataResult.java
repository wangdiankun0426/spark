package com.spark.bean.task.result;

import com.spark.bean.base.BaseResult;
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
public class TaskInstanceDataResult extends BaseResult {

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

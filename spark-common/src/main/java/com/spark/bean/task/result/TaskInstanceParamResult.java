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
 * @since 2026-08-17 17:30:00
 * 通用定时任务参数表
 */
@Data
public class TaskInstanceParamResult extends BaseResult {

    /**
     * 定时任务id
     */
    private Long taskId;

    /**
     * 参数编码
     * TaskParamCode
     */
    private String code;

    /**
     * 参数值
     */
    private String value;

}

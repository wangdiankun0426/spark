package com.spark.common.enums;

import lombok.Getter;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 17:30:00
 * 模型探活健康状态
 */
@Getter
public enum ModelHealthStatusEnum {
    NORMAL(1, "正常"),
    ABNORMAL(2, "异常"),
    ;
    private final Integer value;
    private final String desc;

    ModelHealthStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 按状态值查找枚举
     * @param value 状态值
     * @return 健康状态枚举
     */
    public static ModelHealthStatusEnum indexOf(Integer value) {
        for (ModelHealthStatusEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return NORMAL;
    }
}

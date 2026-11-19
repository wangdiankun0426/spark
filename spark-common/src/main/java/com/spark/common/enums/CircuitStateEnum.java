package com.spark.common.enums;

import lombok.Getter;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 11:00:00
 * 供应商熔断状态
 */
@Getter
public enum CircuitStateEnum {
    CLOSED(1, "正常"),
    OPEN(2, "熔断中"),
    HALF_OPEN(3, "试探中"),
    ;
    private final Integer value;
    private final String desc;

    CircuitStateEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 按状态值查找枚举
     * @param value 状态值
     * @return 熔断状态枚举
     */
    public static CircuitStateEnum indexOf(Integer value) {
        for (CircuitStateEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return CLOSED;
    }
}
package com.outdoor.rental.enums;

import lombok.Getter;

/**
 * 装备实例（SKU）生命周期状态。
 */
@Getter
public enum GearItemStatusEnum {

    AVAILABLE(1, "待租"),
    RENTING(2, "租赁中"),
    PENDING_INSPECTION(3, "归还待检查"),
    MAINTENANCE(4, "清洗/维修中");

    private final int code;
    private final String label;

    GearItemStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static GearItemStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GearItemStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}

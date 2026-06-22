package com.outdoor.rental.enums;

import lombok.Getter;

/**
 * 租赁订单状态。
 */
@Getter
public enum RentalOrderStatusEnum {

    PENDING_PAYMENT(0, "待支付"),
    RENTING(1, "借出中"),
    OVERDUE(2, "已逾期"),
    RETURNED(3, "已归还/待结算"),
    PENDING_INSPECTION(4, "待质检"),
    ABNORMAL(5, "异常/需赔偿"),
    COMPLETED(6, "已完成"),
    CANCELLED(7, "已取消");

    private final int code;
    private final String label;

    RentalOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RentalOrderStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RentalOrderStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}

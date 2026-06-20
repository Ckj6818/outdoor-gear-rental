package com.outdoor.rental.dto;

import lombok.Data;

@Data
public class RentalOrderQueryDTO {

    /** 订单编号（模糊搜索） */
    private String orderNo;

    /** 用户 ID */
    private Long userId;

    /** 装备 ID */
    private Long gearId;

    /** 订单状态：0-待支付，1-借出中，2-已逾期，3-已归还，4-待质检，5-异常完结/需赔偿 */
    private Integer orderStatus;

    /** 当前页码，默认 1 */
    private Long pageNum = 1L;

    /** 每页条数，默认 10 */
    private Long pageSize = 10L;
}

package com.outdoor.rental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InspectOrderDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 质检是否通过：true-无损通过，false-异常/有战损 */
    @NotNull(message = "质检结果不能为空")
    private Boolean isPassed;

    /** 质检备注（异常时可描述损坏情况） */
    private String remark;
}

package com.outdoor.rental.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单结算请求：根据赔偿金计算押金退还并完结订单。
 */
@Data
public class OrderSettleDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 损坏赔偿金，默认按 0 处理 */
    @NotNull(message = "赔偿金不能为空")
    @DecimalMin(value = "0", message = "赔偿金不能为负数")
    private BigDecimal compensationAmount;
}

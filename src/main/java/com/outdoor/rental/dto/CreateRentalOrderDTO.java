package com.outdoor.rental.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRentalOrderDTO {

    @NotNull(message = "装备ID不能为空")
    private Long gearId;

    @NotNull(message = "租赁天数不能为空")
    @Min(value = 1, message = "租赁天数至少为1天")
    private Integer rentalDays;

    /** 是否购买意外损坏豁免金（租金 10%） */
    private Boolean hasDamageWaiver;

    private String remark;
}

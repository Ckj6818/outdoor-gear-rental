package com.outdoor.rental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysGearStatusDTO {

    /** 上架状态：0-下架，1-上架 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}

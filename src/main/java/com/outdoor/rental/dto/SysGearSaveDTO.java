package com.outdoor.rental.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SysGearSaveDTO {

    @NotBlank(message = "装备名称不能为空")
    private String name;

    @NotBlank(message = "品牌不能为空")
    private String brand;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotNull(message = "日租金不能为空")
    @DecimalMin(value = "0.00", message = "日租金不能为负数")
    private BigDecimal dailyRent;

    @NotNull(message = "押金不能为空")
    @DecimalMin(value = "0.00", message = "押金不能为负数")
    private BigDecimal deposit;

    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能为负数")
    private Integer totalStock;

    @Min(value = 0, message = "可用库存不能为负数")
    private Integer availableStock;

    private String imageUrl;
}

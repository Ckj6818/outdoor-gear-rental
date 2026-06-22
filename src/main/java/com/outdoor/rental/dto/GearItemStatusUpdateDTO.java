package com.outdoor.rental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 装备实例状态变更请求。
 *
 * @see com.outdoor.rental.enums.GearItemStatusEnum
 */
@Data
public class GearItemStatusUpdateDTO {

    /**
     * 目标状态：1-待租、2-租赁中、3-归还待检查、4-清洗/维修中
     */
    @NotNull(message = "目标状态不能为空")
    private Integer status;

    /** 变更备注（可选） */
    private String remark;
}

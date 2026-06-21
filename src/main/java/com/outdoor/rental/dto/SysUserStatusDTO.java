package com.outdoor.rental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysUserStatusDTO {

    /** 账号状态：0-禁用，1-正常 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}

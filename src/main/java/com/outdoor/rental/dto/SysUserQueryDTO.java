package com.outdoor.rental.dto;

import lombok.Data;

@Data
public class SysUserQueryDTO {

    /** 用户名（模糊搜索） */
    private String username;

    /** 手机号（精确匹配） */
    private String phone;

    /** 账号状态：0-禁用，1-正常 */
    private Integer status;

    /**
     * 角色筛选：0-管理员，1-普通用户；不传则查询全部
     */
    private Integer role;

    /** 当前页码，默认 1 */
    private Long pageNum = 1L;

    /** 每页条数，默认 10 */
    private Long pageSize = 10L;
}

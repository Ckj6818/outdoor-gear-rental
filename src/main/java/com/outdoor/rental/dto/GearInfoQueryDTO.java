package com.outdoor.rental.dto;

import lombok.Data;

@Data
public class GearInfoQueryDTO {

    /** 装备名称（模糊搜索） */
    private String gearName;

    /** 装备分类（精确筛选） */
    private String category;

    /** 品牌（可选筛选） */
    private String brand;

    /** 上架状态：0-下架，1-上架 */
    private Integer status;

    /** 当前页码，默认 1 */
    private Long pageNum = 1L;

    /** 每页条数，默认 10 */
    private Long pageSize = 10L;
}

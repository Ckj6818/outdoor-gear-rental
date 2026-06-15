package com.outdoor.rental.dto;

import lombok.Data;

@Data
public class GearInfoQueryDTO {

    /** 关键词（模糊匹配装备名称） */
    private String keyword;

    /** 装备名称（模糊搜索，兼容旧接口，keyword 优先） */
    private String gearName;

    /** 装备分类：背包 / 帐篷 / 鞋服 / 配件，或库内精确分类值 */
    private String category;

    /** 品牌（精确筛选） */
    private String brand;

    /** 上架状态：0-下架，1-上架 */
    private Integer status;

    /** 当前页码，默认 1 */
    private Long pageNum = 1L;

    /** 每页条数，默认 10 */
    private Long pageSize = 10L;
}

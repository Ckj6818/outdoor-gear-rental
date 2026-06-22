package com.outdoor.rental.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 导购 RAG 上下文中的装备信息（含描述与参数，供大模型做专业推荐）。
 */
@Data
public class AiRagGearVO {

    private Long id;

    private String name;

    private String brand;

    private String category;

    @JsonProperty("daily_rent")
    private BigDecimal dailyRent;

    @JsonProperty("available_stock")
    private Integer availableStock;

    @JsonProperty("condition_grade")
    private String conditionGrade;

    /** 商品简介（截断后注入，节省 Token） */
    private String description;

    /** 技术参数（截断后注入） */
    private String specifications;
}

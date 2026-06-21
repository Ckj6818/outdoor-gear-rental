package com.outdoor.rental.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 导购 RAG 上下文中的精简装备信息（仅含必要字段以节省 Token）。
 */
@Data
public class AiRagGearVO {

    private Long id;

    private String name;

    private String category;

    @JsonProperty("daily_rent")
    private BigDecimal dailyRent;
}

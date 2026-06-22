package com.outdoor.rental.service;

import com.outdoor.rental.dto.AiChatMessageDTO;
import com.outdoor.rental.vo.AiRagGearVO;

import java.util.List;

/**
 * AI 导购检索式 RAG：根据用户问题与对话历史，从全量库存中筛选最相关装备。
 */
public interface AiGearRagRetriever {

    /**
     * 合并当前问题与历史，供意图识别与参数抽取。
     */
    String mergeContext(String question, List<AiChatMessageDTO> history, int maxHistoryMessages);

    /**
     * 检索与问题最相关的可租装备子集（按相关度排序，限制条数以节省 Token）。
     */
    List<AiRagGearVO> retrieve(String question, List<AiChatMessageDTO> history, List<AiRagGearVO> allGears, int maxItems);

    /** 从合并上下文中抽取容量（升/L） */
    Integer extractCapacityLiters(String text);

    /** 从合并上下文中抽取预算（元） */
    Integer extractBudgetYuan(String text);

    /** 从合并上下文中抽取行程天数（三天两夜等） */
    int extractTripDays(String text);

    /** 默认租期天数（用于预算估算） */
    int extractRentalDays(String text);
}

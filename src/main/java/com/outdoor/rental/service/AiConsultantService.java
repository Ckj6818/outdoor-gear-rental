package com.outdoor.rental.service;

import com.outdoor.rental.dto.AiConsultRequestDTO;

/**
 * AI 户外装备导购服务。
 */
public interface AiConsultantService {

    /**
     * 根据用户提问与对话历史，返回 JSON 字符串（含 reply 与 recommend_gears）。
     */
    String consult(AiConsultRequestDTO request);
}

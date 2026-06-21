package com.outdoor.rental.service;

/**
 * AI 户外装备导购服务。
 */
public interface AiConsultantService {

    /**
     * 根据用户提问返回大模型生成的 JSON 字符串（含 reply 与 recommend_gears）。
     *
     * @param question 用户提问
     * @return 纯 JSON 字符串，由前端自行解析
     */
    String consult(String question);
}

package com.outdoor.rental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.consultant")
public class AiConsultantProperties {

    /** 是否启用 AI 导购（未配置 API Key 时可设为 false） */
    private boolean enabled = true;

    /** OpenAI 兼容接口地址，如 DeepSeek / 通义千问等 */
    private String apiUrl = "https://api.deepseek.com/chat/completions";

    /** 大模型 API Key，建议通过环境变量 AI_API_KEY 注入 */
    private String apiKey = "";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** HTTP 读超时（秒） */
    private int timeoutSeconds = 60;

    /** 未配置 API Key 时是否启用本地 Mock（基于 RAG 装备列表的规则推荐，便于答辩演示）。 */
    private boolean mockFallback = true;

    /** 检索式 RAG 注入大模型的最大装备条数 */
    private int ragMaxItems = 15;

    /** 多轮对话保留的历史消息条数（user+assistant 合计） */
    private int maxHistoryMessages = 8;
}

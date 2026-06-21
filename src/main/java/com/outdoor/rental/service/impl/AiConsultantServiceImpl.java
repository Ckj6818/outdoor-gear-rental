package com.outdoor.rental.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.outdoor.rental.config.AiConsultantProperties;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.SysGearMapper;
import com.outdoor.rental.service.AiConsultantService;
import com.outdoor.rental.vo.AiRagGearVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 导购服务：轻量级 RAG + 大模型结构化输出。
 * <p>
 * 流程：查询可租装备 → 注入 System Prompt → 调用大模型 → 返回纯 JSON 字符串。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConsultantServiceImpl implements AiConsultantService {

    private static final String SYSTEM_PROMPT_PREFIX = """
            你是「山行」户外装备租赁平台的 AI 导购助手。你的职责是根据用户需求，从真实库存中推荐合适的租赁装备，并提供专业、友善的户外建议。

            ## 可租用装备清单（JSON）
            这是目前仓库中可租用的真实装备列表，请严格从该列表中挑选推荐，不得编造列表中不存在的装备 id 或名称：
            """;

    private static final String SYSTEM_PROMPT_SUFFIX = """

            ## 输出格式（必须严格遵守）
            你必须只返回一个 JSON 对象，不要包含 markdown 代码块标记（如 ```json），不要有任何 JSON 以外的文字。
            格式如下：
            {
              "reply": "在这里写你作为导购的自然语言回复、关怀和建议",
              "recommend_gears": [
                {"id": 1, "name": "装备名称", "rent": 50.0}
              ]
            }

            ## 规则
            1. recommend_gears 中的 id、name、rent 必须来自上述装备清单；rent 对应该装备的 daily_rent 日租金数值。
            2. 若用户只是闲聊、问候或问题与装备租赁无关，recommend_gears 返回空数组 []。
            3. 推荐时请结合用户提到的路线、季节、天数、预算等需求，给出 1-3 款最合适装备。
            4. reply 使用中文，语气专业且温暖。
            """;

    private final SysGearMapper sysGearMapper;
    private final AiConsultantProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String consult(String question) {
        if (!aiProperties.isEnabled()) {
            log.warn("AI 导购已禁用，拒绝处理提问");
            throw new BusinessException(503, "AI 导购功能暂未开放");
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            if (aiProperties.isMockFallback()) {
                log.warn("AI 导购 API Key 未配置，启用本地 Mock 模式（可在 ai.consultant.mock-fallback=false 关闭）");
                return consultWithMock(question);
            }
            log.error("AI 导购 API Key 未配置，请在 application-local.yml 或环境变量 AI_API_KEY 中设置");
            throw new BusinessException(503, "AI 导购尚未配置，请联系管理员");
        }

        return consultWithLlm(question);
    }

    private String consultWithMock(String question) {
        List<AiRagGearVO> availableGears = loadAvailableGears(question);
        try {
            String json = buildMockResponse(question, availableGears);
            log.info("AI 导购 Mock 回复成功 questionLength={}", question.length());
            return json;
        } catch (Exception ex) {
            log.error("AI 导购 Mock 回复生成失败 question={}", question, ex);
            throw new BusinessException(500, "AI 导购处理失败，请稍后再试");
        }
    }

    private String consultWithLlm(String question) {
        List<AiRagGearVO> availableGears = loadAvailableGears(question);

        String gearCatalogJson;
        try {
            gearCatalogJson = objectMapper.writeValueAsString(availableGears);
        } catch (Exception ex) {
            log.error("AI 导购装备列表 JSON 序列化失败", ex);
            throw new BusinessException(500, "装备数据处理失败，请稍后再试");
        }

        String systemPrompt = buildSystemPrompt(gearCatalogJson);

        try {
            String rawContent = callLlm(systemPrompt, question);
            String jsonResponse = sanitizeJsonResponse(rawContent);
            log.info("AI 导购调用成功 questionLength={} responseLength={}",
                    question.length(), jsonResponse.length());
            return jsonResponse;
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("AI 导购大模型 HTTP 调用失败 question={} apiUrl={}",
                    question, aiProperties.getApiUrl(), ex);
            throw new BusinessException(502, "AI 导购服务暂时不可用，请稍后再试");
        } catch (Exception ex) {
            log.error("AI 导购大模型调用异常 question={}", question, ex);
            throw new BusinessException(500, "AI 导购处理失败，请稍后再试");
        }
    }

    private List<AiRagGearVO> loadAvailableGears(String question) {
        try {
            List<AiRagGearVO> availableGears = sysGearMapper.selectAvailableOnShelfWithStock();
            log.info("AI 导购 RAG 检索完成，可租装备 {} 件", availableGears.size());
            return availableGears;
        } catch (Exception ex) {
            log.error("AI 导购 RAG 检索装备列表失败 question={}", question, ex);
            throw new BusinessException(500, "装备库存查询失败，请稍后再试");
        }
    }

    /**
     * 无 API Key 时的本地 Mock：基于 RAG 装备列表做关键词匹配，仍返回标准 JSON 结构。
     */
    private String buildMockResponse(String question, List<AiRagGearVO> gears) throws Exception {
        String q = question == null ? "" : question.trim();
        String lower = q.toLowerCase();

        List<AiRagGearVO> matched;
        String reply;

        if (isSmallTalkOnly(lower)) {
            matched = List.of();
            reply = "你好！我是山行 AI 导购。告诉我你的出行计划、路线或预算，我会从当前可租库存里为你推荐装备。"
                    + "（当前为演示 Mock 模式，配置 DeepSeek API Key 后可接入真实大模型）";
        } else if (containsAny(lower, "露营", "帐篷", "过夜")) {
            matched = filterByCategory(gears, "帐篷", 3);
            reply = buildRecommendReply("露营", matched,
                    "以下帐篷均来自当前仓库实时库存，适合徒步露营与轻量化出行。");
        } else if (containsAny(lower, "徒步", "背包", "香山", "凤凰岭")) {
            matched = filterByCategory(gears, "背包", 3);
            reply = buildRecommendReply("一日或轻装徒步", matched,
                    "以下背包按日租价格从库存中筛选，可根据行程天数进一步调整容量选择。");
        } else if (containsAny(lower, "鞋", "徒步鞋", "登山")) {
            matched = filterByCategory(gears, "鞋", 3);
            reply = buildRecommendReply("徒步鞋", matched,
                    "建议根据路况选择中帮防水款或轻量低帮款，确保磨合后再上长线。");
        } else if (containsAny(lower, "睡袋", "保暖")) {
            matched = filterByCategory(gears, "睡袋", 3);
            reply = buildRecommendReply("睡袋", matched,
                    "请结合营地夜间温度选择舒适温标，并注意防潮垫搭配。");
        } else {
            matched = gears.stream().limit(3).toList();
            reply = matched.isEmpty()
                    ? "抱歉，当前暂无可租装备。你可以稍后再来，或到装备大厅浏览最新上架。"
                    : "根据你的描述，我从当前可租库存中挑选了以下装备供参考，欢迎点击「去租赁」查看详情。";
        }

        List<Map<String, Object>> recommendGears = matched.stream()
                .map(gear -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", gear.getId());
                    item.put("name", gear.getName());
                    item.put("rent", gear.getDailyRent());
                    return item;
                })
                .toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("reply", reply);
        payload.put("recommend_gears", recommendGears);
        return objectMapper.writeValueAsString(payload);
    }

    private boolean isSmallTalkOnly(String lower) {
        boolean smallTalk = containsAny(lower, "你好", "您好", "hello", "hi", "谢谢", "再见");
        boolean gearIntent = containsAny(lower, "租", "推荐", "露营", "徒步", "装备", "帐篷", "背包", "鞋");
        return smallTalk && !gearIntent;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<AiRagGearVO> filterByCategory(List<AiRagGearVO> gears, String categoryKeyword, int limit) {
        return gears.stream()
                .filter(gear -> gear.getCategory() != null && gear.getCategory().contains(categoryKeyword))
                .limit(limit)
                .toList();
    }

    private String buildRecommendReply(String scene, List<AiRagGearVO> matched, String tip) {
        if (matched.isEmpty()) {
            return "关于" + scene + "的需求已收到，但当前该类装备暂无可用库存，建议到装备大厅查看其他品类或稍后再试。";
        }
        return "针对" + scene + "场景，为你推荐 " + matched.size() + " 款可租装备。" + tip;
    }

    /**
     * 拼接 System Prompt（使用字符串拼接而非 formatted，避免装备 JSON 中的 % 触发格式化异常）。
     */
    private String buildSystemPrompt(String gearCatalogJson) {
        return SYSTEM_PROMPT_PREFIX + gearCatalogJson + SYSTEM_PROMPT_SUFFIX;
    }

    /**
     * 调用 OpenAI 兼容 Chat Completions 接口。
     */
    private String callLlm(String systemPrompt, String userQuestion) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userQuestion)
        ));
        body.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey().trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                aiProperties.getApiUrl(),
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error("AI 导购大模型返回非 2xx 状态 status={}", response.getStatusCode());
            throw new BusinessException(502, "AI 导购服务响应异常");
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || !StringUtils.hasText(contentNode.asText())) {
            log.error("AI 导购大模型响应缺少 content 字段 body={}", response.getBody());
            throw new BusinessException(502, "AI 导购返回内容为空");
        }
        return contentNode.asText();
    }

    /**
     * 去除大模型可能误加的 markdown 代码块包裹，确保返回纯 JSON 字符串。
     */
    private String sanitizeJsonResponse(String raw) {
        if (raw == null) {
            return "{}";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            trimmed = trimmed.trim();
        }
        return trimmed;
    }
}

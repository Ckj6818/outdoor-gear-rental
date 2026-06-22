package com.outdoor.rental.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.outdoor.rental.config.AiConsultantProperties;
import com.outdoor.rental.dto.AiChatMessageDTO;
import com.outdoor.rental.dto.AiConsultRequestDTO;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.SysGearMapper;
import com.outdoor.rental.service.AiConsultantService;
import com.outdoor.rental.service.AiGearRagRetriever;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 导购服务：轻量级 RAG + 大模型结构化输出。
 * <p>
 * 流程：查询可租装备 → 检索式 RAG 筛选 → 注入 System Prompt → 多轮对话调用大模型 → 返回 JSON。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConsultantServiceImpl implements AiConsultantService {

    private static final int DEFAULT_RENTAL_DAYS = 3;

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
            3. 当用户明确指定装备类型（如背包、帐篷、睡袋、徒步鞋）时，必须优先推荐该类型；不要被「露营」「徒步」等场景词覆盖用户指定的品类。
            4. 推荐时请结合用户提到的路线、季节、天数、预算、容量（如 30L、500 元）等需求，给出 1-3 款最合适装备；reply 中的品类描述必须与 recommend_gears 实际品类一致。
            5. 用户说「包」且未提水袋/睡袋时，按背包理解；若提到升数/L（如 30L），优先推荐名称或品类中容量最接近的背包。
            6. 当用户询问「够不够」「合适吗」「可以吗」等咨询问题时，优先在 reply 中给出专业判断与理由，recommend_gears 可为空 []；只有用户明确要求推荐/租赁时再推商品卡片。
            7. reply 使用中文，语气专业且温暖；先解答疑问，再视情况补充 0-2 款可选装备。
            8. 若存在对话历史，请结合上文理解指代（如「它」「这个容量」「三天两夜」），保持前后一致。
            """;

    private final SysGearMapper sysGearMapper;
    private final AiGearRagRetriever ragRetriever;
    private final AiConsultantProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String consult(AiConsultRequestDTO request) {
        String question = request.getQuestion().trim();
        List<AiChatMessageDTO> history = sanitizeHistory(request.getHistory());

        if (!aiProperties.isEnabled()) {
            log.warn("AI 导购已禁用，拒绝处理提问");
            throw new BusinessException(503, "AI 导购功能暂未开放");
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            if (aiProperties.isMockFallback()) {
                log.warn("AI 导购 API Key 未配置，启用本地 Mock 模式（可在 ai.consultant.mock-fallback=false 关闭）");
                return consultWithMock(question, history);
            }
            log.error("AI 导购 API Key 未配置，请在 application-local.yml 或环境变量 AI_API_KEY 中设置");
            throw new BusinessException(503, "AI 导购尚未配置，请联系管理员");
        }

        return consultWithLlm(question, history);
    }

    private List<AiChatMessageDTO> sanitizeHistory(List<AiChatMessageDTO> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        List<AiChatMessageDTO> cleaned = new ArrayList<>();
        for (AiChatMessageDTO msg : history) {
            if (msg == null || !StringUtils.hasText(msg.getContent()) || !StringUtils.hasText(msg.getRole())) {
                continue;
            }
            String role = msg.getRole().trim().toLowerCase();
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            AiChatMessageDTO item = new AiChatMessageDTO();
            item.setRole(role);
            item.setContent(msg.getContent().trim());
            cleaned.add(item);
        }
        int max = aiProperties.getMaxHistoryMessages();
        if (cleaned.size() > max) {
            return cleaned.subList(cleaned.size() - max, cleaned.size());
        }
        return cleaned;
    }

    private String consultWithMock(String question, List<AiChatMessageDTO> history) {
        List<AiRagGearVO> retrievedGears = loadRetrievedGears(question, history);
        try {
            String json = buildMockResponse(question, history, retrievedGears);
            log.info("AI 导购 Mock 回复成功 questionLength={} historySize={}", question.length(), history.size());
            return json;
        } catch (Exception ex) {
            log.error("AI 导购 Mock 回复生成失败 question={}", question, ex);
            throw new BusinessException(500, "AI 导购处理失败，请稍后再试");
        }
    }

    private String consultWithLlm(String question, List<AiChatMessageDTO> history) {
        List<AiRagGearVO> retrievedGears = loadRetrievedGears(question, history);

        String gearCatalogJson;
        try {
            gearCatalogJson = objectMapper.writeValueAsString(retrievedGears);
        } catch (Exception ex) {
            log.error("AI 导购装备列表 JSON 序列化失败", ex);
            throw new BusinessException(500, "装备数据处理失败，请稍后再试");
        }

        String systemPrompt = buildSystemPrompt(gearCatalogJson);

        try {
            String rawContent = callLlm(systemPrompt, question, history);
            String jsonResponse = sanitizeJsonResponse(rawContent);
            log.info("AI 导购调用成功 questionLength={} historySize={} ragItems={} responseLength={}",
                    question.length(), history.size(), retrievedGears.size(), jsonResponse.length());
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

    private List<AiRagGearVO> loadRetrievedGears(String question, List<AiChatMessageDTO> history) {
        try {
            List<AiRagGearVO> allGears = sysGearMapper.selectAvailableOnShelfWithStock();
            List<AiRagGearVO> retrieved = ragRetriever.retrieve(
                    question, history, allGears, aiProperties.getRagMaxItems());
            log.info("AI 导购 RAG 检索完成，全量 {} 件，注入 {} 件", allGears.size(), retrieved.size());
            return retrieved;
        } catch (Exception ex) {
            log.error("AI 导购 RAG 检索装备列表失败 question={}", question, ex);
            throw new BusinessException(500, "装备库存查询失败，请稍后再试");
        }
    }

    /**
     * 无 API Key 时的本地 Mock：基于 RAG 装备列表做关键词匹配，仍返回标准 JSON 结构。
     */
    private String buildMockResponse(String question, List<AiChatMessageDTO> history, List<AiRagGearVO> gears) throws Exception {
        String q = question == null ? "" : question.trim();
        String lower = q.toLowerCase();
        String mergedContext = ragRetriever.mergeContext(q, history, aiProperties.getMaxHistoryMessages());
        String mergedLower = mergedContext.toLowerCase();

        List<AiRagGearVO> matched;
        String reply;

        if (isSmallTalkOnly(lower)) {
            matched = List.of();
            reply = "你好！我是山行 AI 导购。告诉我你的出行计划、路线或预算，我会从当前可租库存里为你推荐装备。"
                    + "（当前为演示 Mock 模式，配置 DeepSeek API Key 后可接入真实大模型）";
        } else if (isAdvisoryQuestion(lower)) {
            matched = List.of();
            reply = buildAdvisoryReply(q, lower, mergedContext, mergedLower, gears);
        } else {
            MockRecommendIntent intent = resolveMockIntent(q, mergedContext, mergedLower);
            matched = matchGearsForMock(mergedContext, mergedLower, gears, intent);
            reply = buildRecommendReply(intent.sceneLabel(), matched, intent.tip(), mergedContext, mergedLower);
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
        boolean gearIntent = containsAny(lower, "租", "推荐", "露营", "徒步", "装备", "帐篷", "背包", "包", "鞋", "l");
        return smallTalk && !gearIntent;
    }

    private boolean isAdvisoryQuestion(String lower) {
        if (containsAny(lower, "推荐", "租", "有没有", "帮我找", "给我选", "哪个好", "买什么")) {
            return false;
        }
        return containsAny(lower,
                "够吗", "够不够", "足够", "可以吗", "行不行", "合适吗", "合适不",
                "需要吗", "要不要", "怎么样", "如何", "为什么", "什么意思", "能否"
        ) || (lower.contains("吗") && containsAny(lower, "够", "合适", "可以", "行"));
    }

    /**
     * 咨询类问题：先给专业判断，不强行推商品卡片。
     */
    private String buildAdvisoryReply(
            String question,
            String lower,
            String mergedContext,
            String mergedLower,
            List<AiRagGearVO> gears
    ) {
        Integer liters = ragRetriever.extractCapacityLiters(mergedContext);
        if (liters == null) {
            liters = ragRetriever.extractCapacityLiters(question);
        }
        int tripDays = ragRetriever.extractTripDays(mergedContext);
        if (tripDays <= 0) {
            tripDays = ragRetriever.extractTripDays(question);
        }

        if (liters != null && (tripDays > 0 || containsAny(mergedLower, "行程", "天", "夜") || containsAny(lower, "够", "足够"))) {
            return buildBackpackCapacityAdvice(liters, tripDays, mergedLower, gears);
        }
        if (containsAny(mergedLower, "睡袋") && containsAny(mergedLower, "温", "度", "冷", "热")) {
            return "睡袋选型关键看「舒适温标」与营地夜间最低温的匹配，建议舒适温标低于预期夜间温度 5-10°C。"
                    + "若你告诉我出行月份和海拔，我可以帮你判断现有库存里哪几款更合适。";
        }
        return "这个问题更偏向装备咨询。你可以补充出行天数、季节、是否露营过夜、是否需要自带帐篷睡袋，"
                + "我会结合当前可租库存给你更具体的建议；若需要直接选款，也可以说「推荐一个 XX」。";
    }

    private String buildBackpackCapacityAdvice(
            int liters,
            int tripDays,
            String lower,
            List<AiRagGearVO> gears
    ) {
        int days = tripDays > 0 ? tripDays : DEFAULT_RENTAL_DAYS;
        boolean overnight = containsAny(lower, "夜", "露营", "过夜", "扎营");
        boolean selfSufficient = containsAny(lower, "帐篷", "睡袋", "露营") || overnight;

        String verdict;
        if (days <= 1 && !overnight) {
            verdict = liters + "L 一日徒步完全够用，重点带好水、路餐和防雨层即可。";
        } else if (days <= 2 && !selfSufficient) {
            verdict = liters + "L 两天一夜若不住营（或帐篷睡袋另背/共背），通常够用。";
        } else if (days <= 3 && liters >= 28 && liters <= 35 && !selfSufficient) {
            verdict = liters + "L 三天两夜轻装出行（不含帐篷睡袋，或与他人分担大件）基本够用。";
        } else if (days <= 3 && liters >= 28 && liters <= 35 && selfSufficient) {
            verdict = liters + "L 三天两夜若要自己背帐篷+睡袋+食物，会偏紧；建议精简装备，或考虑 35-45L。";
        } else if (liters < 28 && days >= 2 && selfSufficient) {
            verdict = liters + "L 对 " + days + " 天且需自备露营装备来说偏小，容易装不下或背负失衡。";
        } else if (liters >= 40) {
            verdict = liters + "L 应对 " + days + " 天行程比较从容，有余量携带露营与备用衣物。";
        } else {
            verdict = liters + "L 是否够用，取决于你是否自备帐篷睡袋、季节衣物和食物水量；"
                    + "轻装可试，重装建议 35L 以上。";
        }

        String upgradeHint = findUpgradeBackpackHint(liters, days, selfSufficient, gears);
        return verdict + " 判断依据：天数约 " + days + " 天"
                + (overnight ? "、含过夜" : "")
                + (selfSufficient ? "、需背露营大件" : "、轻装/分担大件")
                + "。" + upgradeHint
                + " 需要我直接推荐可租的具体型号，可以说「推荐一个 "
                + Math.max(liters, 30) + "L 左右的包」。";
    }

    private String findUpgradeBackpackHint(int liters, int days, boolean selfSufficient, List<AiRagGearVO> gears) {
        if (!selfSufficient || liters >= 36 || days <= 2) {
            return "";
        }
        List<AiRagGearVO> upgrades = gears.stream()
                .filter(gear -> matchesCategory(gear, "背包"))
                .filter(gear -> {
                    Integer cap = ragRetriever.extractCapacityLiters(gear.getName());
                    return cap != null && cap > liters && cap <= 45;
                })
                .sorted(Comparator.comparingInt(gear -> capacityDistance(gear, Math.min(liters + 10, 40))))
                .limit(1)
                .toList();
        if (upgrades.isEmpty()) {
            return "";
        }
        AiRagGearVO pick = upgrades.get(0);
        String rentText = pick.getDailyRent() == null ? "--" : pick.getDailyRent().stripTrailingZeros().toPlainString();
        return " 若担心装不下，库存里 " + pick.getName() + "（¥" + rentText + "/天）容量更宽裕，可按需租赁。";
    }

    private int extractTripDays(String text) {
        return ragRetriever.extractTripDays(text);
    }

    private MockRecommendIntent resolveMockIntent(String question, String mergedContext, String mergedLower) {
        if (containsAny(mergedLower, "睡袋")) {
            return new MockRecommendIntent("睡袋", "睡袋", MockMatchMode.CATEGORY,
                    "以下睡袋均来自当前仓库实时库存，请结合营地夜间温度选择舒适温标。");
        }
        if (asksForBackpack(mergedLower)) {
            return new MockRecommendIntent("背包", "背包", MockMatchMode.BACKPACK,
                    "以下背包均来自当前仓库实时库存，已尽量匹配你提到的容量与预算。");
        }
        if (containsAny(mergedLower, "帐篷", "帐蓬")) {
            return new MockRecommendIntent("帐篷", "帐篷", MockMatchMode.CATEGORY,
                    "以下帐篷均来自当前仓库实时库存，适合徒步露营与轻量化出行。");
        }
        if (containsAny(mergedLower, "徒步鞋", "登山鞋") || (containsAny(mergedLower, "鞋") && !containsAny(mergedLower, "睡袋"))) {
            return new MockRecommendIntent("徒步鞋", "鞋", MockMatchMode.CATEGORY,
                    "以下徒步鞋均来自当前仓库实时库存，建议根据路况选择中帮防水或轻量低帮款。");
        }
        if (containsAny(mergedLower, "登山杖")) {
            return new MockRecommendIntent("登山杖", "登山杖", MockMatchMode.CATEGORY,
                    "以下登山杖均来自当前仓库实时库存，可减轻下坡对膝盖的冲击。");
        }
        if (containsAny(mergedLower, "炉", "炉灶", "气炉")) {
            return new MockRecommendIntent("炉具", "炉具", MockMatchMode.CATEGORY,
                    "以下炉具均来自当前仓库实时库存，适合露营做饭与烧水。");
        }
        if (containsAny(mergedLower, "头灯")) {
            return new MockRecommendIntent("头灯", "头灯", MockMatchMode.CATEGORY,
                    "以下头灯均来自当前仓库实时库存，露营夜间活动必备。");
        }
        if (containsAny(mergedLower, "冲锋衣", "棉服", "服装", "雨衣")) {
            return new MockRecommendIntent("户外服装", "户外服装", MockMatchMode.CATEGORY,
                    "以下服装均来自当前仓库实时库存，可应对露营时的防风防雨与保暖需求。");
        }
        if (containsAny(mergedLower, "露营", "过夜")) {
            return new MockRecommendIntent("露营", "帐篷", MockMatchMode.CATEGORY,
                    "以下帐篷均来自当前仓库实时库存，适合周末露营；如需背包或睡袋可继续告诉我。");
        }
        if (containsAny(mergedLower, "徒步", "香山", "凤凰岭")) {
            return new MockRecommendIntent("徒步", "背包", MockMatchMode.BACKPACK,
                    "以下背包均来自当前仓库实时库存，可根据行程天数进一步调整容量选择。");
        }
        if (ragRetriever.extractBudgetYuan(mergedContext) != null || ragRetriever.extractCapacityLiters(mergedContext) != null) {
            return new MockRecommendIntent("装备", "背包", MockMatchMode.BACKPACK,
                    "已根据你提到的预算或容量，从背包类库存中为你筛选。");
        }
        return new MockRecommendIntent("综合", "", MockMatchMode.GENERAL,
                "根据你的描述，我从当前可租库存中挑选了以下装备供参考，欢迎点击「去租赁」查看详情。");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private enum MockMatchMode {
        CATEGORY, BACKPACK, GENERAL
    }

    private record MockRecommendIntent(
            String sceneLabel,
            String categoryKeyword,
            MockMatchMode matchMode,
            String tip
    ) {
    }

    private List<AiRagGearVO> matchGearsForMock(
            String mergedContext,
            String mergedLower,
            List<AiRagGearVO> gears,
            MockRecommendIntent intent
    ) {
        return switch (intent.matchMode()) {
            case BACKPACK -> rankBackpacks(mergedContext, gears, 3);
            case CATEGORY -> filterByCategory(gears, intent.categoryKeyword(), 3);
            case GENERAL -> gears.stream().limit(3).toList();
        };
    }

    private boolean asksForBackpack(String lower) {
        if (containsAny(lower, "背包")) {
            return true;
        }
        if (containsAny(lower, "水袋", "睡袋")) {
            return false;
        }
        if (lower.contains("包")) {
            return true;
        }
        return ragRetriever.extractCapacityLiters(lower) != null
                && containsAny(lower, "推荐", "租", "包", "背包")
                && !containsAny(lower, "帐篷", "睡袋", "炉", "鞋");
    }

    private List<AiRagGearVO> rankBackpacks(String mergedContext, List<AiRagGearVO> gears, int limit) {
        Integer targetLiters = ragRetriever.extractCapacityLiters(mergedContext);
        Integer budgetYuan = ragRetriever.extractBudgetYuan(mergedContext);
        int rentalDays = ragRetriever.extractRentalDays(mergedContext);

        List<AiRagGearVO> backpacks = gears.stream()
                .filter(gear -> matchesCategory(gear, "背包"))
                .filter(gear -> withinBudget(gear, budgetYuan, rentalDays))
                .sorted(backpackComparator(targetLiters))
                .limit(limit)
                .toList();

        if (!backpacks.isEmpty()) {
            return backpacks;
        }

        return gears.stream()
                .filter(gear -> matchesCategory(gear, "背包"))
                .sorted(backpackComparator(targetLiters))
                .limit(limit)
                .toList();
    }

    private Comparator<AiRagGearVO> backpackComparator(Integer targetLiters) {
        return Comparator
                .comparingInt((AiRagGearVO gear) -> capacityDistance(gear, targetLiters))
                .thenComparing(gear -> gear.getDailyRent() == null ? BigDecimal.ZERO : gear.getDailyRent());
    }

    private int capacityDistance(AiRagGearVO gear, Integer targetLiters) {
        if (targetLiters == null) {
            return 0;
        }
        Integer capacity = ragRetriever.extractCapacityLiters(gear.getName());
        if (capacity == null) {
            return 500;
        }
        return Math.abs(capacity - targetLiters);
    }

    private List<AiRagGearVO> filterByCategory(List<AiRagGearVO> gears, String categoryKeyword, int limit) {
        if (!StringUtils.hasText(categoryKeyword)) {
            return gears.stream().limit(limit).toList();
        }
        return gears.stream()
                .filter(gear -> matchesCategory(gear, categoryKeyword))
                .limit(limit)
                .toList();
    }

    private boolean matchesCategory(AiRagGearVO gear, String categoryKeyword) {
        if (gear.getCategory() != null && gear.getCategory().contains(categoryKeyword)) {
            return true;
        }
        return gear.getName() != null && gear.getName().contains(categoryKeyword);
    }

    private boolean withinBudget(AiRagGearVO gear, Integer budgetYuan, int rentalDays) {
        if (budgetYuan == null || gear.getDailyRent() == null) {
            return true;
        }
        BigDecimal estimatedTotal = gear.getDailyRent()
                .multiply(BigDecimal.valueOf(rentalDays))
                .setScale(2, RoundingMode.HALF_UP);
        return estimatedTotal.compareTo(BigDecimal.valueOf(budgetYuan)) <= 0;
    }

    private String buildRecommendReply(String scene, List<AiRagGearVO> matched, String tip, String mergedContext, String mergedLower) {
        if (matched.isEmpty()) {
            return "关于" + scene + "的需求已收到，但当前该类装备暂无可用库存，建议到装备大厅查看其他品类或稍后再试。";
        }
        if ("综合".equals(scene)) {
            return tip;
        }
        Integer targetLiters = ragRetriever.extractCapacityLiters(mergedContext);
        Integer budgetYuan = ragRetriever.extractBudgetYuan(mergedContext);
        if (asksForBackpack(mergedLower) && (targetLiters != null || budgetYuan != null)) {
            StringBuilder detail = new StringBuilder("针对你的需求，为你推荐 ")
                    .append(matched.size())
                    .append(" 款背包。");
            if (targetLiters != null) {
                detail.append("已优先匹配约 ").append(targetLiters).append("L 容量；");
            }
            if (budgetYuan != null) {
                detail.append("按约 ").append(budgetYuan).append(" 元、")
                        .append(ragRetriever.extractRentalDays(mergedContext))
                        .append(" 天租期估算也在预算内；");
            }
            detail.append(tip);
            return detail.toString();
        }
        return "针对" + scene + "需求，为你推荐 " + matched.size() + " 款可租装备。" + tip;
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
    private String callLlm(String systemPrompt, String userQuestion, List<AiChatMessageDTO> history) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            for (AiChatMessageDTO msg : history) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", userQuestion));

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", messages);
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

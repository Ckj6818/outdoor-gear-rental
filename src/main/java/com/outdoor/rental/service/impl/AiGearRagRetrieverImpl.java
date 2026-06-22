package com.outdoor.rental.service.impl;

import com.outdoor.rental.dto.AiChatMessageDTO;
import com.outdoor.rental.service.AiGearRagRetriever;
import com.outdoor.rental.vo.AiRagGearVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检索式 RAG：关键词 + 品类 + 容量/预算约束打分，返回 Top-K 装备。
 */
@Service
public class AiGearRagRetrieverImpl implements AiGearRagRetriever {

    private static final Pattern LITER_PATTERN = Pattern.compile("(\\d+)\\s*(?:l|升)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BUDGET_PATTERN = Pattern.compile("(\\d+)\\s*(?:块|元)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DAYS_PATTERN = Pattern.compile("(\\d+)\\s*天");
    private static final int DEFAULT_RENTAL_DAYS = 3;

    @Override
    public String mergeContext(String question, List<AiChatMessageDTO> history, int maxHistoryMessages) {
        StringBuilder sb = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - maxHistoryMessages);
            for (AiChatMessageDTO msg : history.subList(start, history.size())) {
                if (msg != null && StringUtils.hasText(msg.getContent())) {
                    sb.append(msg.getContent().trim()).append(' ');
                }
            }
        }
        if (StringUtils.hasText(question)) {
            sb.append(question.trim());
        }
        return sb.toString().trim();
    }

    @Override
    public List<AiRagGearVO> retrieve(
            String question,
            List<AiChatMessageDTO> history,
            List<AiRagGearVO> allGears,
            int maxItems
    ) {
        if (allGears == null || allGears.isEmpty()) {
            return List.of();
        }
        String context = mergeContext(question, history, 8);
        String lower = context.toLowerCase();
        String categoryHint = resolveCategoryHint(lower);

        Integer targetLiters = extractCapacityLiters(context);
        Integer budgetYuan = extractBudgetYuan(context);
        int rentalDays = extractRentalDays(context);

        List<ScoredGear> scored = new ArrayList<>();
        for (AiRagGearVO gear : allGears) {
            int score = scoreGear(gear, lower, categoryHint, targetLiters, budgetYuan, rentalDays);
            if (score > 0) {
                scored.add(new ScoredGear(gear, score));
            }
        }

        if (scored.isEmpty()) {
            return allGears.stream().limit(maxItems).toList();
        }

        scored.sort(Comparator.comparingInt(ScoredGear::score).reversed());
        return scored.stream()
                .map(ScoredGear::gear)
                .distinct()
                .limit(maxItems)
                .toList();
    }

    private int scoreGear(
            AiRagGearVO gear,
            String lower,
            String categoryHint,
            Integer targetLiters,
            Integer budgetYuan,
            int rentalDays
    ) {
        int score = 1;
        String name = gear.getName() == null ? "" : gear.getName().toLowerCase();
        String category = gear.getCategory() == null ? "" : gear.getCategory().toLowerCase();
        String brand = gear.getBrand() == null ? "" : gear.getBrand().toLowerCase();
        String desc = gear.getDescription() == null ? "" : gear.getDescription().toLowerCase();
        String specs = gear.getSpecifications() == null ? "" : gear.getSpecifications().toLowerCase();
        String blob = name + " " + category + " " + brand + " " + desc + " " + specs;

        if (StringUtils.hasText(categoryHint)) {
            if (category.contains(categoryHint) || name.contains(categoryHint)) {
                score += 80;
            } else {
                score -= 40;
            }
        }

        for (String token : extractQueryTokens(lower)) {
            if (blob.contains(token)) {
                score += 15;
            }
        }

        if (targetLiters != null && categoryHint != null && categoryHint.contains("背包")) {
            Integer cap = extractCapacityLiters(gear.getName());
            if (cap != null) {
                score += Math.max(0, 50 - Math.abs(cap - targetLiters));
            }
        }

        if (budgetYuan != null && withinBudget(gear, budgetYuan, rentalDays)) {
            score += 25;
        } else if (budgetYuan != null && !withinBudget(gear, budgetYuan, rentalDays)) {
            score -= 30;
        }

        if (gear.getAvailableStock() != null && gear.getAvailableStock() > 0) {
            score += 5;
        }

        return score;
    }

    private String resolveCategoryHint(String lower) {
        if (containsAny(lower, "睡袋")) {
            return "睡袋";
        }
        if (asksForBackpack(lower)) {
            return "背包";
        }
        if (containsAny(lower, "帐篷", "帐蓬")) {
            return "帐篷";
        }
        if (containsAny(lower, "徒步鞋", "登山鞋") || (containsAny(lower, "鞋") && !containsAny(lower, "睡袋"))) {
            return "鞋";
        }
        if (containsAny(lower, "登山杖")) {
            return "登山杖";
        }
        if (containsAny(lower, "炉", "炉灶", "气炉")) {
            return "炉具";
        }
        if (containsAny(lower, "头灯")) {
            return "头灯";
        }
        if (containsAny(lower, "冲锋衣", "棉服", "服装", "雨衣")) {
            return "户外服装";
        }
        if (containsAny(lower, "水具", "水袋", "滤水")) {
            return "水";
        }
        if (containsAny(lower, "露营", "过夜") && !containsAny(lower, "背包", "包")) {
            return "帐篷";
        }
        if (containsAny(lower, "徒步", "香山", "凤凰岭")) {
            return "背包";
        }
        return "";
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
        return extractCapacityLiters(lower) != null
                && containsAny(lower, "推荐", "租", "包", "背包")
                && !containsAny(lower, "帐篷", "睡袋", "炉", "鞋");
    }

    private List<String> extractQueryTokens(String lower) {
        List<String> tokens = new ArrayList<>();
        String[] keywords = {
                "露营", "徒步", "香山", "凤凰岭", "轻量化", "重装", "防水", "gtx",
                "osprey", "msr", "patagonia", "arc", "garmin", "gps"
        };
        for (String kw : keywords) {
            if (lower.contains(kw)) {
                tokens.add(kw);
            }
        }
        return tokens;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer extractCapacityLiters(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        Matcher matcher = LITER_PATTERN.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    @Override
    public Integer extractBudgetYuan(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        Matcher matcher = BUDGET_PATTERN.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    @Override
    public int extractRentalDays(String text) {
        if (!StringUtils.hasText(text)) {
            return DEFAULT_RENTAL_DAYS;
        }
        if (text.contains("三天两夜") || text.contains("3天2夜")) {
            return 3;
        }
        if (text.contains("两天一夜") || text.contains("2天1夜")) {
            return 2;
        }
        Matcher matcher = DAYS_PATTERN.matcher(text);
        if (matcher.find()) {
            return Math.max(1, Integer.parseInt(matcher.group(1)));
        }
        return DEFAULT_RENTAL_DAYS;
    }

    @Override
    public int extractTripDays(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        if (text.contains("三天两夜") || text.contains("3天2夜")) {
            return 3;
        }
        if (text.contains("两天一夜") || text.contains("2天1夜")) {
            return 2;
        }
        Matcher matcher = DAYS_PATTERN.matcher(text);
        if (matcher.find()) {
            return Math.max(1, Integer.parseInt(matcher.group(1)));
        }
        if (text.contains("两夜")) {
            return 2;
        }
        if (text.contains("一夜")) {
            return 1;
        }
        return 0;
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

    private record ScoredGear(AiRagGearVO gear, int score) {
    }
}

package com.outdoor.rental.config;

import com.outdoor.rental.dto.GearInfoQueryDTO;

/**
 * 装备查询参数归一化，保证相同语义请求命中同一缓存 Key
 */
public final class GearInfoQueryNormalizer {

    private GearInfoQueryNormalizer() {
    }

    public static GearInfoQueryDTO normalize(GearInfoQueryDTO query) {
        GearInfoQueryDTO normalized = new GearInfoQueryDTO();
        if (query == null) {
            normalized.setPageNum(1L);
            normalized.setPageSize(10L);
            return normalized;
        }

        normalized.setKeyword(trimToNull(query.getKeyword()));
        normalized.setGearName(trimToNull(query.getGearName()));
        normalized.setCategory(trimToNull(query.getCategory()));
        normalized.setBrand(trimToNull(query.getBrand()));
        normalized.setStatus(query.getStatus());

        Long pageNum = query.getPageNum();
        normalized.setPageNum(pageNum == null || pageNum < 1 ? 1L : pageNum);

        Long pageSize = query.getPageSize();
        normalized.setPageSize(pageSize == null || pageSize < 1 ? 10L : pageSize);

        return normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

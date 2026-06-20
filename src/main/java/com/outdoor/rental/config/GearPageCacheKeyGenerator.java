package com.outdoor.rental.config;

import com.outdoor.rental.dto.GearInfoQueryDTO;
import org.springframework.stereotype.Component;

/**
 * 装备分页查询缓存 Key 生成器（与 {@link com.outdoor.rental.service.impl.GearInfoServiceImpl} 查询归一化规则一致）
 */
@Component("gearPageCacheKeyGenerator")
public class GearPageCacheKeyGenerator {

    public String generate(GearInfoQueryDTO query) {
        GearInfoQueryDTO normalized = GearInfoQueryNormalizer.normalize(query);
        return String.join(":",
                nullToEmpty(normalized.getKeyword()),
                nullToEmpty(normalized.getGearName()),
                nullToEmpty(normalized.getCategory()),
                nullToEmpty(normalized.getBrand()),
                normalized.getStatus() == null ? "" : normalized.getStatus().toString(),
                normalized.getPageNum().toString(),
                normalized.getPageSize().toString()
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

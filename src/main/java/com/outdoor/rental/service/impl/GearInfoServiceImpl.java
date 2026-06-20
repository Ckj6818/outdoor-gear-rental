package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.config.GearInfoQueryNormalizer;
import com.outdoor.rental.config.RedisConfig;
import com.outdoor.rental.dto.GearInfoQueryDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.service.GearInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class GearInfoServiceImpl implements GearInfoService {

    private static final BigDecimal DEPRECIATION_RATE = new BigDecimal("0.90");
    private static final int DEPRECIATION_MILESTONE = 5;

    private final GearInfoMapper gearInfoMapper;

    @Override
    @Cacheable(cacheNames = RedisConfig.GEAR_PAGE_CACHE, key = "@gearPageCacheKeyGenerator.generate(#query)")
    public PageResult<GearInfo> pageQuery(GearInfoQueryDTO query) {
        GearInfoQueryDTO normalized = GearInfoQueryNormalizer.normalize(query);
        Page<GearInfo> page = new Page<>(normalized.getPageNum(), normalized.getPageSize());
        IPage<GearInfo> result = gearInfoMapper.selectGearPage(page, normalized);
        return PageResult.of(result);
    }

    @Override
    public GearInfo getById(Long id) {
        GearInfo gearInfo = gearInfoMapper.selectById(id);
        if (gearInfo == null) {
            throw new BusinessException(404, "装备不存在");
        }
        return gearInfo;
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public void create(GearInfo gearInfo) {
        validateGearInfo(gearInfo);
        if (gearInfo.getStatus() == null) {
            gearInfo.setStatus(1);
        }
        if (gearInfo.getAvailableStock() == null) {
            gearInfo.setAvailableStock(gearInfo.getTotalStock());
        }
        gearInfoMapper.insert(gearInfo);
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public void update(GearInfo gearInfo) {
        if (gearInfo.getId() == null) {
            throw new BusinessException(400, "装备ID不能为空");
        }
        getById(gearInfo.getId());
        validateGearInfo(gearInfo);
        gearInfoMapper.updateById(gearInfo);
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public void deleteById(Long id) {
        getById(id);
        gearInfoMapper.deleteById(id);
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public void evictPageCache() {
        // 注解驱动清空缓存，方法体 intentionally empty
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public void applyLifecycleAfterInspectionPass(Long gearId) {
        int rows = gearInfoMapper.incrementRentCount(gearId);
        if (rows == 0) {
            throw new BusinessException(400, "装备不存在");
        }

        GearInfo gear = gearInfoMapper.selectById(gearId);
        if (gear == null) {
            throw new BusinessException(400, "装备不存在");
        }

        if (gear.getRentCount() != null && gear.getRentCount() == DEPRECIATION_MILESTONE) {
            applyDepreciation(gear);
            gearInfoMapper.updateById(gear);
            log.info("装备 [{}] 累计出借 {} 次，触发自动折旧：成色={}，日租金={}",
                    gear.getGearName(), gear.getRentCount(), gear.getConditionGrade(), gear.getDailyRent());
        }
    }

    private void applyDepreciation(GearInfo gear) {
        String grade = gear.getConditionGrade();
        if ("全新".equals(grade)) {
            gear.setConditionGrade("9成新");
        } else if ("9成新".equals(grade)) {
            gear.setConditionGrade("轻微使用痕迹");
        }

        if (gear.getDailyRent() != null) {
            gear.setDailyRent(gear.getDailyRent()
                    .multiply(DEPRECIATION_RATE)
                    .setScale(2, RoundingMode.HALF_UP));
        }
    }

    private void validateGearInfo(GearInfo gearInfo) {
        if (!StringUtils.hasText(gearInfo.getGearName())) {
            throw new BusinessException(400, "装备名称不能为空");
        }
        if (!StringUtils.hasText(gearInfo.getBrand())) {
            throw new BusinessException(400, "品牌不能为空");
        }
        if (!StringUtils.hasText(gearInfo.getCategory())) {
            throw new BusinessException(400, "装备分类不能为空");
        }
        if (gearInfo.getDailyRent() == null || gearInfo.getDailyRent().signum() < 0) {
            throw new BusinessException(400, "日租金不能为负数");
        }
        if (gearInfo.getTotalStock() == null || gearInfo.getTotalStock() < 0) {
            throw new BusinessException(400, "总库存量不能为负数");
        }
        if (gearInfo.getAvailableStock() != null && gearInfo.getAvailableStock() > gearInfo.getTotalStock()) {
            throw new BusinessException(400, "可用库存不能大于总库存");
        }
    }
}

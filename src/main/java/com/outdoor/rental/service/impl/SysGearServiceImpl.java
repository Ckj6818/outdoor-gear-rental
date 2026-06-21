package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.config.RedisConfig;
import com.outdoor.rental.dto.SysGearQueryDTO;
import com.outdoor.rental.dto.SysGearSaveDTO;
import com.outdoor.rental.entity.SysGear;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.SysGearMapper;
import com.outdoor.rental.service.SysGearService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysGearServiceImpl implements SysGearService {

    private static final int STATUS_OFF = 0;
    private static final int STATUS_ON = 1;

    private final SysGearMapper sysGearMapper;

    @Override
    public PageResult<SysGear> pageQuery(SysGearQueryDTO query) {
        SysGearQueryDTO params = normalizeQuery(query);

        LambdaQueryWrapper<SysGear> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(params.getName()), SysGear::getName, params.getName())
                .eq(StringUtils.hasText(params.getCategory()), SysGear::getCategory, params.getCategory())
                .eq(params.getStatus() != null, SysGear::getStatus, params.getStatus())
                .orderByDesc(SysGear::getCreateTime);

        Page<SysGear> page = new Page<>(params.getPageNum(), params.getPageSize());
        IPage<SysGear> result = sysGearMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public SysGear create(SysGearSaveDTO dto) {
        validateStock(dto.getTotalStock(), dto.getAvailableStock());

        SysGear gear = new SysGear();
        applySaveFields(gear, dto);
        gear.setStatus(STATUS_ON);
        if (gear.getAvailableStock() == null) {
            gear.setAvailableStock(gear.getTotalStock());
        }

        sysGearMapper.insert(gear);
        return gear;
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public SysGear update(Long id, SysGearSaveDTO dto) {
        SysGear existing = getById(id);
        validateStock(dto.getTotalStock(), dto.getAvailableStock());
        applySaveFields(existing, dto);
        if (existing.getAvailableStock() == null) {
            existing.setAvailableStock(existing.getTotalStock());
        }

        sysGearMapper.updateById(existing);
        return existing;
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public void deleteById(Long id) {
        getById(id);
        sysGearMapper.deleteById(id);
    }

    @Override
    @CacheEvict(cacheNames = RedisConfig.GEAR_PAGE_CACHE, allEntries = true)
    public SysGear updateStatus(Long id, Integer status) {
        if (status == null || (status != STATUS_OFF && status != STATUS_ON)) {
            throw new BusinessException(400, "状态值无效，仅支持 0-下架 或 1-上架");
        }

        SysGear existing = getById(id);
        existing.setStatus(status);
        sysGearMapper.updateById(existing);
        return existing;
    }

    private SysGear getById(Long id) {
        SysGear gear = sysGearMapper.selectById(id);
        if (gear == null) {
            throw new BusinessException(404, "装备不存在");
        }
        return gear;
    }

    private void applySaveFields(SysGear gear, SysGearSaveDTO dto) {
        gear.setName(dto.getName().trim());
        gear.setBrand(dto.getBrand().trim());
        gear.setCategory(dto.getCategory().trim());
        gear.setDailyRent(dto.getDailyRent());
        gear.setDeposit(dto.getDeposit());
        gear.setTotalStock(dto.getTotalStock());
        gear.setAvailableStock(dto.getAvailableStock());
        gear.setImageUrl(trimToNull(dto.getImageUrl()));
    }

    private void validateStock(Integer totalStock, Integer availableStock) {
        if (totalStock == null || totalStock < 0) {
            throw new BusinessException(400, "总库存不能为负数");
        }
        if (availableStock != null && availableStock > totalStock) {
            throw new BusinessException(400, "可用库存不能大于总库存");
        }
    }

    private SysGearQueryDTO normalizeQuery(SysGearQueryDTO query) {
        SysGearQueryDTO params = query != null ? query : new SysGearQueryDTO();
        if (params.getPageNum() == null || params.getPageNum() < 1) {
            params.setPageNum(1L);
        }
        if (params.getPageSize() == null || params.getPageSize() < 1) {
            params.setPageSize(10L);
        }
        if (params.getPageSize() > 100) {
            params.setPageSize(100L);
        }
        return params;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

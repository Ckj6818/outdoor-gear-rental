package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.GearInfoQueryDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.service.GearInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GearInfoServiceImpl implements GearInfoService {

    private final GearInfoMapper gearInfoMapper;

    @Override
    public PageResult<GearInfo> pageQuery(GearInfoQueryDTO query) {
        Page<GearInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<GearInfo> result = gearInfoMapper.selectGearPage(page, query);
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
    public void update(GearInfo gearInfo) {
        if (gearInfo.getId() == null) {
            throw new BusinessException(400, "装备ID不能为空");
        }
        getById(gearInfo.getId());
        validateGearInfo(gearInfo);
        gearInfoMapper.updateById(gearInfo);
    }

    @Override
    public void deleteById(Long id) {
        getById(id);
        gearInfoMapper.deleteById(id);
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

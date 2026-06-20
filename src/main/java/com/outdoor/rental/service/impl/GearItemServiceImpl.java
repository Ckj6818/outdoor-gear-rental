package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.entity.GearItem;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.mapper.GearItemMapper;
import com.outdoor.rental.service.GearItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GearItemServiceImpl implements GearItemService {

    private static final int STATUS_IN_STOCK = 0;

    private final GearItemMapper gearItemMapper;
    private final GearInfoMapper gearInfoMapper;

    @Override
    public GearItem getById(Long id) {
        GearItem gearItem = gearItemMapper.selectById(id);
        if (gearItem == null) {
            throw new BusinessException(404, "装备实例不存在");
        }
        return gearItem;
    }

    @Override
    public List<GearItem> listByGearId(Long gearId) {
        return gearItemMapper.selectList(
                new LambdaQueryWrapper<GearItem>()
                        .eq(GearItem::getGearId, gearId)
                        .orderByAsc(GearItem::getId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateItemsForGear(Long gearId, int count) {
        if (count <= 0) {
            throw new BusinessException(400, "生成数量必须大于0");
        }
        GearInfo gearInfo = gearInfoMapper.selectById(gearId);
        if (gearInfo == null) {
            throw new BusinessException(404, "装备不存在");
        }
        for (int i = 0; i < count; i++) {
            GearItem item = new GearItem();
            item.setGearId(gearId);
            item.setSnCode(buildSnCode(gearId));
            item.setStatus(STATUS_IN_STOCK);
            gearItemMapper.insert(item);
        }
    }

    private String buildSnCode(Long gearId) {
        String uuidPrefix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "SN-" + gearId + "-" + uuidPrefix;
    }
}

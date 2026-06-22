package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outdoor.rental.dto.GearItemStatusUpdateDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.entity.GearItem;
import com.outdoor.rental.enums.GearItemStatusEnum;
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
            item.setStatus(GearItemStatusEnum.AVAILABLE.getCode());
            gearItemMapper.insert(item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GearItem updateStatus(Long itemId, GearItemStatusUpdateDTO dto) {
        GearItem gearItem = getById(itemId);
        Integer targetStatus = dto.getStatus();
        if (!GearItemStatusEnum.isValid(targetStatus)) {
            throw new BusinessException(400, "无效的装备状态，仅支持 1-待租、2-租赁中、3-归还待检查、4-清洗/维修中");
        }
        if (targetStatus.equals(gearItem.getStatus())) {
            return gearItem;
        }

        gearItem.setStatus(targetStatus);
        gearItemMapper.updateById(gearItem);
        return gearItem;
    }

    private String buildSnCode(Long gearId) {
        String uuidPrefix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "SN-" + gearId + "-" + uuidPrefix;
    }
}

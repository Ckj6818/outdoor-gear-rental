package com.outdoor.rental.service;

import com.outdoor.rental.entity.GearItem;

import java.util.List;

public interface GearItemService {

    GearItem getById(Long id);

    List<GearItem> listByGearId(Long gearId);

    /**
     * 为指定装备批量生成装备实例（SKU/SN）
     *
     * @param gearId 装备 SPU ID
     * @param count  生成数量
     */
    void generateItemsForGear(Long gearId, int count);
}

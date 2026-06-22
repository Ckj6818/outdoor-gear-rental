package com.outdoor.rental.service;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.GearInfoQueryDTO;
import com.outdoor.rental.entity.GearInfo;

public interface GearInfoService {

    PageResult<GearInfo> pageQuery(GearInfoQueryDTO query);

    GearInfo getById(Long id);

    void create(GearInfo gearInfo);

    void update(GearInfo gearInfo);

    void deleteById(Long id);

    /** 库存或列表数据变更后清空装备大厅 Redis 分页缓存 */
    void evictPageCache();

    /** 质检通过后更新装备生命周期（累计出借次数与自动折旧） */
    void applyLifecycleAfterInspectionPass(Long gearId);
}

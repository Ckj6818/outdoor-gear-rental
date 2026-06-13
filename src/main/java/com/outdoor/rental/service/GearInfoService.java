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
}

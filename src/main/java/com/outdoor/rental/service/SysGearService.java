package com.outdoor.rental.service;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.SysGearQueryDTO;
import com.outdoor.rental.dto.SysGearSaveDTO;
import com.outdoor.rental.entity.SysGear;

public interface SysGearService {

    PageResult<SysGear> pageQuery(SysGearQueryDTO query);

    SysGear create(SysGearSaveDTO dto);

    SysGear update(Long id, SysGearSaveDTO dto);

    void deleteById(Long id);

    SysGear updateStatus(Long id, Integer status);
}

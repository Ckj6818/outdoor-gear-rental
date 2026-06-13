package com.outdoor.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.dto.GearInfoQueryDTO;
import com.outdoor.rental.entity.GearInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GearInfoMapper extends BaseMapper<GearInfo> {

    /**
     * 多条件复合分页查询装备列表
     */
    IPage<GearInfo> selectGearPage(Page<GearInfo> page, @Param("query") GearInfoQueryDTO query);

    /**
     * 原子扣减可用库存（available_stock > 0 时才扣减）
     *
     * @return 受影响行数，0 表示库存不足
     */
    int deductAvailableStock(@Param("gearId") Long gearId);
}

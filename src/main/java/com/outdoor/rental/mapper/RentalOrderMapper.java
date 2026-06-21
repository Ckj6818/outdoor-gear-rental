package com.outdoor.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.RentalOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RentalOrderMapper extends BaseMapper<RentalOrder> {

    /**
     * 多条件复合分页查询订单列表
     */
    IPage<RentalOrder> selectOrderPage(Page<RentalOrder> page, @Param("query") RentalOrderQueryDTO query);

    /**
     * 查询某装备当前仍占用档期的有效订单（借出中 / 已逾期 / 待质检）。
     */
    List<RentalOrder> selectActiveOccupiedOrdersByGearId(@Param("gearId") Long gearId);
}

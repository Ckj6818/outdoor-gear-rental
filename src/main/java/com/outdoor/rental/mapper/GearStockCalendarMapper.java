package com.outdoor.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outdoor.rental.entity.GearStockCalendar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface GearStockCalendarMapper extends BaseMapper<GearStockCalendar> {

    /**
     * 锁定指定装备在日期区间内的日历库存行（含首尾日期，需在事务内调用）。
     */
    List<GearStockCalendar> selectForUpdateByGearAndDateRange(
            @Param("gearId") Long gearId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 乐观锁扣减单日库存：stock - count >= 0 且 version 匹配时成功。
     *
     * @param oldVersion 扣减前读到的版本号
     * @return 受影响行数，0 表示库存不足或版本冲突
     */
    int deductStockWithVersion(
            @Param("id") Long id,
            @Param("count") Integer count,
            @Param("oldVersion") Integer oldVersion);
}

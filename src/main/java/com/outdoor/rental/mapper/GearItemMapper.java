package com.outdoor.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outdoor.rental.entity.GearItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GearItemMapper extends BaseMapper<GearItem> {

    /**
     * 锁定一条在库实例（需在事务内调用，配合 FOR UPDATE 防并发抢占）
     */
    GearItem selectOneAvailableForUpdate(@Param("gearId") Long gearId);

    /**
     * 将在库实例标记为借出中
     *
     * @return 受影响行数，0 表示实例状态已变更
     */
    int markAsRented(@Param("id") Long id);

    /**
     * 将借出中实例标记为待质检
     *
     * @return 受影响行数，0 表示实例状态已变更
     */
    int markAsPendingInspection(@Param("id") Long id);

    /**
     * 将待质检实例标记为在库（质检通过）
     *
     * @return 受影响行数，0 表示实例状态已变更
     */
    int markInspectionPassed(@Param("id") Long id);

    /**
     * 将待质检实例标记为维修中（质检异常）
     *
     * @return 受影响行数，0 表示实例状态已变更
     */
    int markAsRepairing(@Param("id") Long id);
}

package com.outdoor.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;

/**
 * 装备库存日历：按 gear_id + date 记录当日剩余可租库存。
 */
@Data
@TableName("gear_stock_calendar")
public class GearStockCalendar {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long gearId;

    @TableField("`date`")
    private LocalDate stockDate;

    private Integer stock;

    @Version
    private Integer version;
}

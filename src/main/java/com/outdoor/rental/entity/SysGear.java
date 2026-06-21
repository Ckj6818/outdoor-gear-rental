package com.outdoor.rental.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 户外装备管理实体，映射 gear_info 表。
 */
@Data
@TableName("gear_info")
public class SysGear {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("gear_name")
    private String name;

    private String brand;

    private String category;

    @TableField("daily_rent")
    private BigDecimal dailyRent;

    private BigDecimal deposit;

    @TableField("total_stock")
    private Integer totalStock;

    @TableField("available_stock")
    private Integer availableStock;

    /** 上架状态：0-下架，1-上架 */
    private Integer status;

    @TableField("main_image")
    private String imageUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

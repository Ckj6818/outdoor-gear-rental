package com.outdoor.rental.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("gear_info")
public class GearInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String gearName;

    private String brand;

    private String category;

    private BigDecimal dailyRent;

    private BigDecimal deposit;

    private Integer totalStock;

    private Integer availableStock;

    private String description;

    /** 技术参数(以分号分隔) */
    private String specifications;

    /** 使用须知及注意事项 */
    private String usageInstructions;

    private String mainImage;

    private String hoverImage;

    /** 装备成色：全新 / 9成新 / 轻微使用痕迹 */
    private String conditionGrade;

    /** 累计出借次数（质检通过后累加，用于自动折旧） */
    private Integer rentCount;

    /** 上架状态：0-下架，1-上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

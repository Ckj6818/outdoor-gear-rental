package com.outdoor.rental.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

import com.outdoor.rental.enums.GearItemStatusEnum;

@Data
@TableName("gear_item")
public class GearItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联装备 SPU ID（gear_info.id） */
    private Long gearId;

    /** 唯一序列号 */
    private String snCode;

    /**
     * 实例状态，取值见 {@link GearItemStatusEnum}：
     * 1-待租、2-租赁中、3-归还待检查、4-清洗/维修中
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

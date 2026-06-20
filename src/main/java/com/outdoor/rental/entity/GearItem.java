package com.outdoor.rental.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gear_item")
public class GearItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联装备 SPU ID（gear_info.id） */
    private Long gearId;

    /** 唯一序列号 */
    private String snCode;

    /** 实例状态：0-在库，1-借出中，2-待质检，3-维修中，4-报废 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

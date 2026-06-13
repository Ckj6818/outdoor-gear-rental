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
@TableName("rental_order")
public class RentalOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long gearId;

    private Integer rentalDays;

    private LocalDateTime rentOutTime;

    private LocalDateTime expectedReturnTime;

    private LocalDateTime actualReturnTime;

    /** 订单状态：0-待支付，1-借出中，2-已逾期，3-已归还 */
    private Integer orderStatus;

    private BigDecimal totalFee;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.outdoor.rental.vo;

import lombok.Data;

@Data
public class CategoryShareVO {

    /** 装备分类 */
    private String category;

    /** 出借次数 */
    private Integer count;
}

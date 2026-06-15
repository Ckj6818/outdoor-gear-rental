package com.outdoor.rental.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private Long total;
    private Long pageNum;
    private Long pageSize;
    private List<T> records;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setRecords(page.getRecords());
        return pageResult;
    }
}

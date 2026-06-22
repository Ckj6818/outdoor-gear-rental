package com.outdoor.rental.exception;

/**
 * 日历库存扣减失败：库存不足或并发冲突导致乐观锁更新失败。
 */
public class StockDeductionException extends BusinessException {

    public StockDeductionException(Integer code, String message) {
        super(code, message);
    }

    public static StockDeductionException insufficient(String message) {
        return new StockDeductionException(400, message);
    }

    public static StockDeductionException busy() {
        return new StockDeductionException(409, "系统繁忙，库存扣减冲突，请稍后重试");
    }
}

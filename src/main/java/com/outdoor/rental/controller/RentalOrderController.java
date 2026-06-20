package com.outdoor.rental.controller;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.dto.InspectOrderDTO;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.RentalOrder;
import com.outdoor.rental.service.RentalOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class RentalOrderController {

    private final RentalOrderService rentalOrderService;

    /**
     * 分页查询订单列表
     * GET /api/orders?userId=2&orderStatus=1&pageNum=1&pageSize=10
     */
    @GetMapping
    public Result<PageResult<RentalOrder>> page(RentalOrderQueryDTO query) {
        return Result.success(rentalOrderService.pageQuery(query));
    }

    /**
     * 根据 ID 查询订单详情
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public Result<RentalOrder> getById(@PathVariable Long id) {
        return Result.success(rentalOrderService.getById(id));
    }

    /**
     * 租赁下单（需携带 JWT Token）
     * POST /api/orders
     * Header: Authorization: Bearer {token}
     */
    @PostMapping
    public Result<RentalOrder> createRentalOrder(@RequestBody @Valid CreateRentalOrderDTO dto) {
        RentalOrder order = rentalOrderService.createRentalOrder(dto);
        return Result.success("下单成功", order);
    }

    /**
     * 模拟支付
     * PUT /api/orders/{id}/pay
     */
    @PutMapping("/{id}/pay")
    public Result<RentalOrder> payOrder(@PathVariable Long id) {
        RentalOrder order = rentalOrderService.payOrder(id);
        return Result.success("支付成功", order);
    }

    /**
     * 归还装备
     * PUT /api/orders/{id}/return
     */
    @PutMapping("/{id}/return")
    public Result<RentalOrder> returnGear(@PathVariable Long id) {
        RentalOrder order = rentalOrderService.returnGear(id);
        return Result.success("归还成功", order);
    }

    /**
     * 更新订单
     * PUT /api/orders/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RentalOrder rentalOrder) {
        rentalOrder.setId(id);
        rentalOrderService.update(rentalOrder);
        return Result.success("更新成功", null);
    }

    /**
     * 删除订单
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        rentalOrderService.deleteById(id);
        return Result.success("删除成功", null);
    }
}

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
class AdminRentalOrderController {

    private final RentalOrderService rentalOrderService;

    /**
     * 管理员全量订单分页查询
     * GET /api/admin/orders?orderStatus=4&pageNum=1&pageSize=10
     */
    @GetMapping
    public Result<PageResult<RentalOrder>> page(RentalOrderQueryDTO query) {
        return Result.success(rentalOrderService.adminPageQuery(query));
    }

    /**
     * 管理员质检闭环
     * POST /api/admin/orders/inspect
     */
    @PostMapping("/inspect")
    public Result<RentalOrder> inspectOrder(@RequestBody @Valid InspectOrderDTO dto) {
        RentalOrder order = rentalOrderService.inspectOrder(dto);
        return Result.success("质检完成", order);
    }
}

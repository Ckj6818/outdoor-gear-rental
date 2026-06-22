package com.outdoor.rental.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.outdoor.rental.annotation.LogOperation;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.dto.InspectOrderDTO;
import com.outdoor.rental.dto.OrderSettleDTO;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.RentalOrder;
import com.outdoor.rental.service.RentalOrderService;
import com.outdoor.rental.vo.OccupiedDateRangeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@Tag(name = "租赁订单", description = "用户下单、支付、归还等订单流程")
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
     * 查询某装备已被占用的租赁档期（供前端日历禁用已租日期）
     * GET /api/orders/occupied-dates/{gearId}
     */
    @GetMapping("/occupied-dates/{gearId}")
    public Result<List<OccupiedDateRangeVO>> listOccupiedDates(@PathVariable Long gearId) {
        return Result.success(rentalOrderService.listOccupiedDates(gearId));
    }

    /**
     * 根据 ID 查询订单详情
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public Result<RentalOrder> getById(@PathVariable Long id) {
        return Result.success(rentalOrderService.getById(id));
    }

    @Operation(summary = "租赁下单", description = "锁定库存并创建待支付订单，15 分钟内未支付将自动取消。需在 Header 携带 Authorization: Bearer {token}")
    @PostMapping
    @LogOperation("租赁下单")
    public Result<RentalOrder> createRentalOrder(@RequestBody @Valid CreateRentalOrderDTO dto) {
        RentalOrder order = rentalOrderService.createRentalOrder(dto);
        return Result.success("下单成功", order);
    }

    @Operation(summary = "模拟支付", description = "待支付订单支付成功后进入借出中状态，并移除超时取消计时")
    @PutMapping("/{id}/pay")
    public Result<RentalOrder> payOrder(
            @Parameter(description = "订单 ID", required = true, example = "1001")
            @PathVariable Long id) {
        RentalOrder order = rentalOrderService.payOrder(id);
        return Result.success("支付成功", order);
    }

    /**
     * 归还装备
     * PUT /api/orders/{id}/return
     */
    @PutMapping("/{id}/return")
    @LogOperation("归还装备")
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
    @SaCheckRole("ADMIN")
    public Result<PageResult<RentalOrder>> page(RentalOrderQueryDTO query) {
        return Result.success(rentalOrderService.adminPageQuery(query));
    }

    /**
     * 管理员质检闭环
     * POST /api/admin/orders/inspect
     */
    @PostMapping("/inspect")
    @SaCheckRole("ADMIN")
    @LogOperation("管理员质检")
    public Result<RentalOrder> inspectOrder(@RequestBody @Valid InspectOrderDTO dto) {
        RentalOrder order = rentalOrderService.inspectOrder(dto);
        return Result.success("质检完成", order);
    }

    /**
     * 订单结算：计算押金退还并完结订单
     * POST /api/admin/orders/settle
     * <p>
     * 实际退还 = 押金 - 赔偿金；订单状态更新为「已完成」(6)
     * </p>
     */
    @PostMapping("/settle")
    @SaCheckRole("ADMIN")
    @LogOperation("订单结算")
    public Result<RentalOrder> settleOrder(@RequestBody @Valid OrderSettleDTO dto) {
        RentalOrder order = rentalOrderService.settleOrder(dto);
        return Result.success("结算完成", order);
    }
}

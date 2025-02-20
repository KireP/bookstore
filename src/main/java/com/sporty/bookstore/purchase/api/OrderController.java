package com.sporty.bookstore.purchase.api;

import com.sporty.bookstore.config.ApiConstants;
import com.sporty.bookstore.purchase.dto.request.OrderRequestDto;
import com.sporty.bookstore.purchase.dto.response.OrderPaymentResponseDto;
import com.sporty.bookstore.purchase.dto.response.OrderPriceCalculationResponseDto;
import com.sporty.bookstore.purchase.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstants.ORDERS_API_URI)
@RestController
@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(ApiConstants.ORDER_SUMMARISE_PRICE_API_URI)
    @PreAuthorize(ApiConstants.ADMIN_OR_USER_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN, ROLE_USER] Calculate price for given order.")
    public OrderPriceCalculationResponseDto summariseOrder(@RequestBody @Valid OrderRequestDto request) {
        return orderService.summariseOrder(request);
    }

    @PostMapping(ApiConstants.ORDER_PURCHASE_API_URI)
    @PreAuthorize(ApiConstants.ADMIN_OR_USER_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN, ROLE_USER] Purchase order. Loyalty points will be updated.")
    public OrderPaymentResponseDto purchaseOrder(@RequestBody @Valid OrderRequestDto request) {
        return orderService.purchaseOrder(request);
    }
}

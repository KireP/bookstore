package com.sporty.bookstore.purchase.api;

import com.sporty.bookstore.config.ApiConstants;
import com.sporty.bookstore.purchase.dto.response.LoyaltyPointsResponseDto;
import com.sporty.bookstore.purchase.service.LoyaltyPointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RequestMapping(ApiConstants.LOYALTY_POINTS_API_URI)
@RestController
@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
public class LoyaltyPointsController {

    private final LoyaltyPointsService loyaltyPointsService;

    @GetMapping(ApiConstants.PERSONAL_LOYALTY_POINTS_API_URI)
    @PreAuthorize(ApiConstants.ADMIN_OR_USER_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN, ROLE_USER] Retrieves the logged-in user's loyalty points.")
    public LoyaltyPointsResponseDto getMyLoyaltyPoints() {
        return loyaltyPointsService.getLoyaltyPointsForLoggedInUser();
    }

    @GetMapping("/{userId}")
    @PreAuthorize(ApiConstants.ADMIN_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN] Retrieves any user's loyalty points.")
    public LoyaltyPointsResponseDto getLoyaltyPoints(@PathVariable("userId") BigInteger userId) {
        return loyaltyPointsService.getLoyaltyPointsByUserId(userId);
    }
}

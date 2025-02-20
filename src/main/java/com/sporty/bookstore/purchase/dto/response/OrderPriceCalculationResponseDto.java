package com.sporty.bookstore.purchase.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPriceCalculationResponseDto {

    private Double priceToPay;
    private Boolean loyaltyPointsToBeApplied;
}

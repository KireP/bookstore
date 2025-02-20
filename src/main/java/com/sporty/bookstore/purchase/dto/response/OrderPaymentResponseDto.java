package com.sporty.bookstore.purchase.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPaymentResponseDto {

    private Double pricePaid;
    private Integer loyaltyPointsAfterPurchase;
    private Boolean loyaltyPointsApplied;
}

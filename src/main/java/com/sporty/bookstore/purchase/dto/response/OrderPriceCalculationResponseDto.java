package com.sporty.bookstore.purchase.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPriceCalculationResponseDto {

    private Double priceToPay;
    private Boolean loyaltyPointsToBeApplied;
    private DeductedBookResponseDto bookToBeDeducted;
    private List<BookOrderDetailsResponseDto> books;
}

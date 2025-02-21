package com.sporty.bookstore.purchase.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookOrderDetailsResponseDto {

    private BigInteger id;
    private String title;
    private Double originalPrice;
    private Double priceAfterDiscount;
    private Integer quantity;
}

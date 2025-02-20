package com.sporty.bookstore.purchase.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookQuantityRequestDto {

    @NotNull
    private BigInteger bookId;

    @NotNull
    @Positive
    private Integer quantity;
}

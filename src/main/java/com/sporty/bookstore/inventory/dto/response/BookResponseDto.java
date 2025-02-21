package com.sporty.bookstore.inventory.dto.response;

import com.sporty.bookstore.annotation.api.SortBy;
import com.sporty.bookstore.inventory.enumeration.BookType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
public class BookResponseDto {

    @SortBy(name = "id")
    private BigInteger id;

    @SortBy(name = "type")
    private BookType type;

    @SortBy(name = "creationDate")
    private LocalDateTime creationDate;

    @SortBy(name = "title")
    private String title;

    @SortBy(name = "author")
    private String author;

    @SortBy(name = "price")
    private Double price;
}

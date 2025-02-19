package com.sporty.bookstore.inventory.dto.request;

import com.sporty.bookstore.inventory.enumeration.BookType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBookRequestDto {

    @NotNull
    private BookType type;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotNull
    @Positive
    private Double price;
}

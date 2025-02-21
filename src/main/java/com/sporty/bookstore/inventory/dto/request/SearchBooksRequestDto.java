package com.sporty.bookstore.inventory.dto.request;

import com.sporty.bookstore.annotation.validation.AnyFieldSet;
import com.sporty.bookstore.inventory.enumeration.BookType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@AnyFieldSet(fields = {
        SearchBooksRequestDto.Fields.ids,
        SearchBooksRequestDto.Fields.types,
        SearchBooksRequestDto.Fields.creationDateFrom,
        SearchBooksRequestDto.Fields.creationDateTo,
        SearchBooksRequestDto.Fields.title,
        SearchBooksRequestDto.Fields.author,
        SearchBooksRequestDto.Fields.priceFrom,
        SearchBooksRequestDto.Fields.priceTo
})
@ParameterObject
public class SearchBooksRequestDto {

    Collection<BigInteger> ids;

    Collection<BookType> types;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime creationDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime creationDateTo;

    String title;

    String author;

    Double priceFrom;

    Double priceTo;
}

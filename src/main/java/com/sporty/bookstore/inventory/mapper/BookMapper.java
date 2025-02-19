package com.sporty.bookstore.inventory.mapper;

import com.sporty.bookstore.inventory.dto.request.CreateBookRequestDto;
import com.sporty.bookstore.inventory.dto.response.BookResponseDto;
import com.sporty.bookstore.inventory.entity.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponseDto toBookResponseDto(Book book);

    Book toBookEntity(CreateBookRequestDto createBookRequestDto);
}
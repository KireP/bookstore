package com.sporty.bookstore.inventory.mapper;

import com.sporty.bookstore.inventory.dto.request.CreateBookRequestDto;
import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BookMapperImpl.class)
class BookMapperTest {

    @Autowired
    private BookMapper bookMapper;

    @Test
    void testMapBookToBookResponseDto() {
        var book = Book.builder()
                .id(BigInteger.ONE)
                .type(BookType.REGULAR)
                .creationDate(LocalDateTime.now())
                .title("Witcher")
                .author("Andrzej Sapkowski")
                .price(50D)
                .build();

        var result = bookMapper.toBookResponseDto(book);

        assertEquals(result.getId(), book.getId());
        assertEquals(result.getType(), book.getType());
        assertEquals(result.getCreationDate(), book.getCreationDate());
        assertEquals(result.getTitle(), book.getTitle());
        assertEquals(result.getAuthor(), book.getAuthor());
        assertEquals(result.getPrice(), book.getPrice());
    }

    @Test
    void testMapCreateBookRequestDtoToBook() {
        var createBookRequestDto = CreateBookRequestDto.builder()
                .type(BookType.REGULAR)
                .title("The Kingkiller Chronicle")
                .author("Patrick Rothfuss")
                .price(50D)
                .build();

        var result = bookMapper.toBookEntity(createBookRequestDto);

        assertEquals(result.getType(), createBookRequestDto.getType());
        assertEquals(result.getTitle(), createBookRequestDto.getTitle());
        assertEquals(result.getAuthor(), createBookRequestDto.getAuthor());
        assertEquals(result.getPrice(), createBookRequestDto.getPrice());
    }
}

package com.sporty.bookstore.inventory.service;

import com.sporty.bookstore.exception.BookCannotBeDeletedException;
import com.sporty.bookstore.exception.BookNotFoundException;
import com.sporty.bookstore.inventory.dto.annotation.SortBy;
import com.sporty.bookstore.inventory.dto.request.CreateBookRequestDto;
import com.sporty.bookstore.inventory.dto.request.SearchBooksRequestDto;
import com.sporty.bookstore.inventory.dto.request.UpdateBookRequestDto;
import com.sporty.bookstore.inventory.dto.response.BookResponseDto;
import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import com.sporty.bookstore.inventory.mapper.BookMapper;
import com.sporty.bookstore.inventory.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public Page<BookResponseDto> searchBooks(
            SearchBooksRequestDto request,
            int page,
            int size,
            String sortingColumn,
            Sort.Direction sortingDirection
    ) {
        var sortingColumnOptional = getBooksSortingColumn(sortingColumn);
        if (sortingColumnOptional.isEmpty()) {
            sortingColumn = BookResponseDto.Fields.creationDate;
            sortingDirection = Sort.Direction.DESC;
        }
        return bookRepository
                .searchBooks(
                        request.getIds(),
                        request.getTypes(),
                        request.getCreationDateFrom(),
                        request.getCreationDateTo(),
                        request.getTitle(),
                        request.getAuthor(),
                        request.getPriceFrom(),
                        request.getPriceTo(),
                        PageRequest.of(page, size, Sort.by(sortingDirection, sortingColumn))
                )
                .map(bookMapper::toBookResponseDto);
    }

    public BookResponseDto getBook(BigInteger id) {
        var book = getBookEntity(id);
        return bookMapper.toBookResponseDto(book);
    }

    public BookResponseDto createBook(CreateBookRequestDto createBookRequestDto) {
        var book = bookMapper.toBookEntity(createBookRequestDto);
        book = bookRepository.save(book);
        return bookMapper.toBookResponseDto(book);
    }

    @Transactional
    public BookResponseDto updateBook(BigInteger id, UpdateBookRequestDto updateBookRequestDto) {
        var book = getBookEntity(id);
        book.setType(updateBookRequestDto.getType());
        book.setTitle(updateBookRequestDto.getTitle());
        book.setAuthor(updateBookRequestDto.getAuthor());
        book.setPrice(updateBookRequestDto.getPrice());
        book = bookRepository.save(book);
        return bookMapper.toBookResponseDto(book);
    }

    @Transactional
    public void deleteBook(BigInteger id) {
        var book = getBookEntity(id);
        if (book.getType() != BookType.OLD_EDITION) {
            throw new BookCannotBeDeletedException(id);
        }
        bookRepository.delete(book);
    }

    private Optional<String> getBooksSortingColumn(String sortingColumn) {
        if (Objects.nonNull(sortingColumn)) {
            try {
                var field = BookResponseDto.class.getDeclaredField(sortingColumn);
                if (field.isAnnotationPresent(SortBy.class)) {
                    return Optional.ofNullable(field.getAnnotation(SortBy.class)).map(SortBy::name);
                }
            } catch (NoSuchFieldException ignored) {
            }
        }
        return Optional.empty();
    }

    private Book getBookEntity(BigInteger id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}

package com.sporty.bookstore.inventory.api;

import com.sporty.bookstore.config.ApiConstants;
import com.sporty.bookstore.inventory.dto.request.CreateBookRequestDto;
import com.sporty.bookstore.inventory.dto.request.SearchBooksRequestDto;
import com.sporty.bookstore.inventory.dto.request.UpdateBookRequestDto;
import com.sporty.bookstore.inventory.dto.response.BookResponseDto;
import com.sporty.bookstore.inventory.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RequestMapping(ApiConstants.BOOKS_API_URI)
@RestController
@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    @PreAuthorize(ApiConstants.ADMIN_OR_USER_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN, ROLE_USER] Retrieves books by various criteria.")
    public Page<BookResponseDto> searchBooks(
            @Valid SearchBooksRequestDto request,
            @RequestParam(name = "page") @Min(0) int page,
            @RequestParam(name = "size") @Min(0) @Max(100) int size,
            @RequestParam(name = "sortingColumn") String sortingColumn,
            @RequestParam(name = "sortingDirection") Sort.Direction sortingDirection
    ) {
        return bookService.searchBooks(request, page, size, sortingColumn, sortingDirection);
    }

    @GetMapping("/{bookId}")
    @PreAuthorize(ApiConstants.ADMIN_OR_USER_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN, ROLE_USER] Retrieves book by ID.")
    public BookResponseDto getBook(@PathVariable("bookId") BigInteger bookId) {
        return bookService.getBook(bookId);
    }

    @PostMapping
    @PreAuthorize(ApiConstants.ADMIN_PRE_AUTHORIZATION)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[ROLE_ADMIN] Creates book.")
    public BookResponseDto createBook(@RequestBody @Valid CreateBookRequestDto request) {
        return bookService.createBook(request);
    }

    @PutMapping("/{bookId}")
    @PreAuthorize(ApiConstants.ADMIN_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN] Updates book.")
    public BookResponseDto updateBook(
            @PathVariable("bookId") BigInteger bookId,
            @RequestBody @Valid UpdateBookRequestDto request
    ) {
        return bookService.updateBook(bookId, request);
    }

    @DeleteMapping("/{bookId}")
    @PreAuthorize(ApiConstants.ADMIN_PRE_AUTHORIZATION)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "[ROLE_ADMIN] Deletes book.")
    public void deleteBook(@PathVariable("bookId") BigInteger bookId) {
        bookService.deleteBook(bookId);
    }
}

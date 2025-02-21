package com.sporty.bookstore.inventory.integration;

import com.sporty.bookstore.helper.IntegrationTest;
import com.sporty.bookstore.inventory.dto.request.CreateBookRequestDto;
import com.sporty.bookstore.inventory.dto.request.UpdateBookRequestDto;
import com.sporty.bookstore.inventory.dto.response.BookResponseDto;
import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import com.sporty.bookstore.inventory.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static com.sporty.bookstore.config.ApiConstants.BOOKS_API_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerTest extends IntegrationTest {

    private static final String BOOKS_ENDPOINT = BOOKS_API_URI;
    private static final String SINGLE_BOOK_ENDPOINT = BOOKS_API_URI + "/{bookId}";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void testSearchBooksEndpoint_returnsStatusOK() throws Exception {
        var books = transactionTemplate.execute(x -> List.of(
                createBook(BookType.REGULAR, "Title 1 part 1", "Author 1 senior", 65D),
                createBook(BookType.NEW_RELEASE, "Title 1 part 2", "Author 1 senior", 45D),
                createBook(BookType.OLD_EDITION, "Title 1 part 3", "Author 1 senior", 70D),
                createBook(BookType.NEW_RELEASE, "Title 1 part 4", "Author 1 junior", 80D),
                createBook(BookType.NEW_RELEASE, "Title 2", "Author 1 senior", 65D),
                createBook(BookType.OLD_EDITION, "Title 3", "Author 2", 90D),
                createBook(BookType.NEW_RELEASE, "Title 4", "Author 3", 100D),
                createBook(BookType.REGULAR, "Title 5", "Author 4", 110D),
                createBook(BookType.REGULAR, "Title 6", "Author 5", 120D)
        ));
        assertThat(books).isNotNull();

        var bookIds = books.stream().map(book -> book.getId().toString()).toList();

        mockMvc.perform(
                        get(BOOKS_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .queryParam("ids", String.join(",", bookIds.subList(0, bookIds.size() - 1)))
                                .queryParam("types", BookType.REGULAR + "," + BookType.NEW_RELEASE)
                                .queryParam("creationDateFrom", LocalDateTime.now().minusDays(1).toString())
                                .queryParam("creationDateTo", LocalDateTime.now().plusDays(1).toString())
                                .queryParam("title", "Title 1") // Testing substring search
                                .queryParam("author", "Author 1 sen") // // Testing substring search
                                .queryParam("priceFrom", "40")
                                .queryParam("priceTo", "65")
                                .queryParam("page", "0")
                                .queryParam("size", "10")
                                .queryParam("sortingColumn", BookResponseDto.Fields.price)
                                .queryParam("sortingDirection", Sort.Direction.ASC.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.[0].id").value(books.get(1).getId()))
                .andExpect(jsonPath("$.content.[0].type").value(books.get(1).getType().toString()))
                .andExpect(jsonPath("$.content.[0].title").value(books.get(1).getTitle()))
                .andExpect(jsonPath("$.content.[0].author").value(books.get(1).getAuthor()))
                .andExpect(jsonPath("$.content.[0].price").value(books.get(1).getPrice()))
                .andExpect(jsonPath("$.content.[0].creationDate").isNotEmpty())
                .andExpect(jsonPath("$.content.[1].id").value(books.get(0).getId()))
                .andExpect(jsonPath("$.content.[1].type").value(books.get(0).getType().toString()))
                .andExpect(jsonPath("$.content.[1].title").value(books.get(0).getTitle()))
                .andExpect(jsonPath("$.content.[1].author").value(books.get(0).getAuthor()))
                .andExpect(jsonPath("$.content.[1].price").value(books.get(0).getPrice()))
                .andExpect(jsonPath("$.content.[1].creationDate").isNotEmpty())
                .andExpect(jsonPath("$.content.[2]").doesNotExist());
    }

    @Test
    void testSearchBooksEndpoint_withNoQueryParameters_returnsStatusBadRequest() throws Exception {
        mockMvc.perform(
                        get(BOOKS_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchBooksEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        mockMvc.perform(get(BOOKS_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetBookEndpoint_returnsStatusOK() throws Exception {
        var book = createBook(BookType.REGULAR, "Test title 1", "Test author 1", 65D);

        mockMvc.perform(
                        get(SINGLE_BOOK_ENDPOINT, book.getId())
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value(book.getType().toString()))
                .andExpect(jsonPath("$.title").value(book.getTitle()))
                .andExpect(jsonPath("$.author").value(book.getAuthor()))
                .andExpect(jsonPath("$.price").value(book.getPrice()))
                .andExpect(jsonPath("$.creationDate").isNotEmpty());
    }

    @Test
    void testGetBookEndpoint_withNonexistentBookId_returnsStatusNotFound() throws Exception {
        mockMvc.perform(
                        get(SINGLE_BOOK_ENDPOINT, 123456)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBookEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        mockMvc.perform(get(SINGLE_BOOK_ENDPOINT, 1))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateBookEndpoint_asAdminUser_returnsStatusCreated() throws Exception {
        var request = CreateBookRequestDto.builder()
                .type(BookType.OLD_EDITION)
                .title("Random title")
                .author("Random author")
                .price(43D)
                .build();

        mockMvc.perform(
                        post(BOOKS_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value(request.getType().toString()))
                .andExpect(jsonPath("$.title").value(request.getTitle()))
                .andExpect(jsonPath("$.author").value(request.getAuthor()))
                .andExpect(jsonPath("$.price").value(request.getPrice()))
                .andExpect(jsonPath("$.creationDate").isNotEmpty());
    }

    @Test
    void testCreateBookEndpoint_withMissingData_asAdminUser_returnsStatusBadRequest() throws Exception {
        var request = CreateBookRequestDto.builder()
                .title("Random title 1")
                .author("Random author 1")
                .price(43D)
                .build();

        mockMvc.perform(
                        post(BOOKS_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBookEndpoint_withNegativePrice_asAdminUser_returnsStatusBadRequest() throws Exception {
        var request = CreateBookRequestDto.builder()
                .type(BookType.REGULAR)
                .title("Random title 2")
                .title("Random title 2")
                .author("Random author 2")
                .price(-43D)
                .build();

        mockMvc.perform(
                        post(BOOKS_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBookEndpoint_asRegularUser_returnsStatusForbidden() throws Exception {
        var request = CreateBookRequestDto.builder()
                .type(BookType.REGULAR)
                .title("Random title 3")
                .title("Random title 3")
                .author("Random author 3")
                .price(100D)
                .build();

        mockMvc.perform(
                        post(BOOKS_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateBookEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        var request = CreateBookRequestDto.builder()
                .type(BookType.REGULAR)
                .title("Random title 4")
                .title("Random title 4")
                .author("Random author 4")
                .price(100D)
                .build();

        mockMvc.perform(
                        post(BOOKS_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateBookEndpoint_asAdminUser_returnsStatusOK() throws Exception {
        var book = createBook(BookType.REGULAR, "Old title", "Old author", 65D);
        var request = UpdateBookRequestDto.builder()
                .type(BookType.NEW_RELEASE)
                .title("New title")
                .author("New author")
                .price(43D)
                .build();

        mockMvc.perform(
                        put(SINGLE_BOOK_ENDPOINT, book.getId())
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book.getId()))
                .andExpect(jsonPath("$.type").value(request.getType().toString()))
                .andExpect(jsonPath("$.title").value(request.getTitle()))
                .andExpect(jsonPath("$.author").value(request.getAuthor()))
                .andExpect(jsonPath("$.price").value(request.getPrice()))
                .andExpect(jsonPath("$.creationDate").isNotEmpty());
    }

    @Test
    void testUpdateBookEndpoint_withMissingData_asAdminUser_returnsStatusBadRequest() throws Exception {
        var book = createBook(BookType.REGULAR, "Old title 1", "Old author 1", 65D);
        var request = UpdateBookRequestDto.builder()
                .type(BookType.NEW_RELEASE)
                .author("New author 1")
                .price(43D)
                .build();

        mockMvc.perform(
                        put(SINGLE_BOOK_ENDPOINT, book.getId())
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBookEndpoint_withNegativePrice_asAdminUser_returnsStatusBadRequest() throws Exception {
        var book = createBook(BookType.REGULAR, "Old title 2", "Old author 2", 65D);
        var request = UpdateBookRequestDto.builder()
                .type(BookType.NEW_RELEASE)
                .title("New title 2")
                .author("New author 2")
                .price(-43D)
                .build();

        mockMvc.perform(
                        put(SINGLE_BOOK_ENDPOINT, book.getId())
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBookEndpoint_withNonexistentBookId_asAdminUser_returnsStatusNotFound() throws Exception {
        var request = UpdateBookRequestDto.builder()
                .type(BookType.NEW_RELEASE)
                .title("Test_title")
                .author("Test_author")
                .price(43D)
                .build();

        mockMvc.perform(
                        put(SINGLE_BOOK_ENDPOINT, 123456)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateBookEndpoint_asRegularUser_returnsStatusForbidden() throws Exception {
        var book = createBook(BookType.REGULAR, "Old title 3", "Old author 3", 65D);
        var request = UpdateBookRequestDto.builder()
                .type(BookType.NEW_RELEASE)
                .title("New title 3")
                .author("New author 3")
                .price(43D)
                .build();

        mockMvc.perform(
                        put(SINGLE_BOOK_ENDPOINT, book.getId())
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateBookEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        var book = createBook(BookType.REGULAR, "Old title 4", "Old author 4", 65D);
        var request = UpdateBookRequestDto.builder()
                .type(BookType.NEW_RELEASE)
                .title("New title 4")
                .author("New author 4")
                .price(43D)
                .build();

        mockMvc.perform(
                        put(SINGLE_BOOK_ENDPOINT, book.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteBookEndpoint_asAdminUser_returnsStatusNoContent() throws Exception {
        var book = createBook(BookType.OLD_EDITION, "Some title", "Some author", 65D);

        mockMvc.perform(
                        delete(SINGLE_BOOK_ENDPOINT, book.getId())
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteBookEndpoint_withBookThatIsNotOldEdition_asAdminUser_returnsStatusBadRequest() throws Exception {
        var book = createBook(BookType.REGULAR, "Some title 1", "Some author 1", 65D);

        mockMvc.perform(
                        delete(SINGLE_BOOK_ENDPOINT, book.getId())
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteBookEndpoint_withNonexistentBookId_asAdminUser_returnsStatusNotFound() throws Exception {
        mockMvc.perform(
                        delete(SINGLE_BOOK_ENDPOINT, 123456)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBookEndpoint_asRegularUser_returnsStatusForbidden() throws Exception {
        mockMvc.perform(
                        delete(SINGLE_BOOK_ENDPOINT, 1)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteBookEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        mockMvc.perform(delete(SINGLE_BOOK_ENDPOINT, 1))
                .andExpect(status().isForbidden());
    }

    private Book createBook(BookType bookType, String title, String author, Double price) {
        var book = Book.builder()
                .type(bookType)
                .title(title)
                .author(author)
                .price(price)
                .build();
        return bookRepository.save(book);
    }
}

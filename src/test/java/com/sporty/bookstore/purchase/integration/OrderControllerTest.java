package com.sporty.bookstore.purchase.integration;

import com.sporty.bookstore.helper.AuthHeaderService;
import com.sporty.bookstore.helper.IntegrationTest;
import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import com.sporty.bookstore.inventory.repository.BookRepository;
import com.sporty.bookstore.purchase.dto.request.BookQuantityRequestDto;
import com.sporty.bookstore.purchase.dto.request.OrderRequestDto;
import com.sporty.bookstore.purchase.entity.UserLoyaltyPoints;
import com.sporty.bookstore.purchase.repository.UserLoyaltyPointsRepository;
import com.sporty.bookstore.user.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigInteger;
import java.util.List;

import static com.sporty.bookstore.config.ApiConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends IntegrationTest {

    private static final String SUMMARISE_ORDER_ENDPOINT = ORDERS_API_URI + ORDER_SUMMARISE_PRICE_API_URI;
    private static final String PURCHASE_ORDER_ENDPOINT = ORDERS_API_URI + ORDER_PURCHASE_API_URI;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserLoyaltyPointsRepository userLoyaltyPointsRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Value("${loyalty-points.max}")
    private int maxLoyaltyPoints;

    @Test
    void testSummariseOrderEndpoint_loyaltyPointsApplied_returnsStatusOK() throws Exception {
        var books = transactionTemplate.execute(x -> List.of(
                createBook(BookType.REGULAR, "T1", "A1", 50D),
                createBook(BookType.NEW_RELEASE, "T2", "A2", 100D),
                createBook(BookType.OLD_EDITION, "T3", "A2", 30D)
        ));
        assertThat(books).isNotNull();

        setLoyaltyPointsToUser(AuthHeaderService.REGULAR_USER_USERNAME, maxLoyaltyPoints);

        var request = OrderRequestDto.builder()
                .order(
                        List.of(
                                BookQuantityRequestDto.builder()
                                        .bookId(books.get(0).getId())
                                        .quantity(3)
                                        .build(),
                                BookQuantityRequestDto.builder()
                                        .bookId(books.get(1).getId())
                                        .quantity(3)
                                        .build(),
                                BookQuantityRequestDto.builder()
                                        .bookId(books.get(2).getId())
                                        .quantity(2)
                                        .build()
                        )
                )
                .build();

        var priceToPay =
                // This is REGULAR book.
                // We take into consideration its 10%-deducted-price (it's in a bundle with 3 or more books) multiplied by the quantity.
                // One instance of the most expensive (REGULAR or OLD_EDITION) book in the order is free (loyalty points applicable). That is why we multiply by 2 instead of 3.
                books.get(0).getPrice() * 0.9 * 2 +
                        // This is NEW_RELEASE book.
                        // We take into consideration its full price multiplied by the quantity (3).
                        books.get(1).getPrice() * 3 +
                        // This is OLD_EDITION book.
                        // We take into consideration its 20%-deducted-price with additional 5% deduction (it's in a bundle with 3 or more books) multiplied by the quantity (2).
                        books.get(2).getPrice() * 0.75 * 2;

        mockMvc.perform(
                        post(SUMMARISE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceToPay").value(priceToPay))
                .andExpect(jsonPath("$.loyaltyPointsToBeApplied").value(true))
                .andExpect(jsonPath("$.bookToBeDeducted.id").value(books.get(0).getId()))
                .andExpect(jsonPath("$.bookToBeDeducted.title").value(books.get(0).getTitle()))
                .andExpect(jsonPath("$.bookToBeDeducted.deductedPrice").value(books.get(0).getPrice() * 0.9))
                .andExpect(jsonPath("$.books.[0].id").value(books.get(0).getId()))
                .andExpect(jsonPath("$.books.[0].title").value(books.get(0).getTitle()))
                .andExpect(jsonPath("$.books.[0].originalPrice").value(books.get(0).getPrice()))
                .andExpect(jsonPath("$.books.[0].priceAfterDiscount").value(books.get(0).getPrice() * 0.9))
                .andExpect(jsonPath("$.books.[0].quantity").value(3))
                .andExpect(jsonPath("$.books.[1].id").value(books.get(1).getId()))
                .andExpect(jsonPath("$.books.[1].title").value(books.get(1).getTitle()))
                .andExpect(jsonPath("$.books.[1].originalPrice").value(books.get(1).getPrice()))
                .andExpect(jsonPath("$.books.[1].priceAfterDiscount").value(books.get(1).getPrice()))
                .andExpect(jsonPath("$.books.[1].quantity").value(3))
                .andExpect(jsonPath("$.books.[2].id").value(books.get(2).getId()))
                .andExpect(jsonPath("$.books.[2].title").value(books.get(2).getTitle()))
                .andExpect(jsonPath("$.books.[2].originalPrice").value(books.get(2).getPrice()))
                .andExpect(jsonPath("$.books.[2].priceAfterDiscount").value(books.get(2).getPrice() * 0.75))
                .andExpect(jsonPath("$.books.[2].quantity").value(2))
                .andExpect(jsonPath("$.books.[3]").doesNotExist());
    }

    @Test
    void testSummariseOrderEndpoint_loyaltyPointsNotApplied_returnsStatusOK() throws Exception {
        var book = transactionTemplate.execute(x -> createBook(BookType.OLD_EDITION, "T4", "A4", 50D));
        assertThat(book).isNotNull();

        setLoyaltyPointsToUser(AuthHeaderService.ADMIN_USER_USERNAME, 0);

        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(book.getId())
                                .quantity(2)
                                .build()
                ))
                .build();

        var priceToPay =
                // This is OLD_EDITION book.
                // We take into consideration its 20%-deducted-price multiplied by the quantity (2).
                book.getPrice() * 0.8 * 2;

        mockMvc.perform(
                        post(SUMMARISE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceToPay").value(priceToPay))
                .andExpect(jsonPath("$.loyaltyPointsToBeApplied").value(false))
                .andExpect(jsonPath("$.bookToBeDeducted").doesNotExist())
                .andExpect(jsonPath("$.books.[0].id").value(book.getId()))
                .andExpect(jsonPath("$.books.[0].title").value(book.getTitle()))
                .andExpect(jsonPath("$.books.[0].originalPrice").value(book.getPrice()))
                .andExpect(jsonPath("$.books.[0].priceAfterDiscount").value(book.getPrice() * 0.8))
                .andExpect(jsonPath("$.books.[0].quantity").value(2))
                .andExpect(jsonPath("$.books.[1]").doesNotExist());
    }

    @Test
    void testSummariseOrderEndpoint_withMissingData_returnsStatusBadRequest() throws Exception {
        mockMvc.perform(
                        post(SUMMARISE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(OrderRequestDto.builder().order(List.of()).build()))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSummariseOrderEndpoint_withNegativeQuantity_returnsStatusBadRequest() throws Exception {
        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(BigInteger.ONE)
                                .quantity(-1)
                                .build()
                ))
                .build();

        mockMvc.perform(
                        post(SUMMARISE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSummariseOrderEndpoint_withNonexistentBookId_returnsStatusNotFound() throws Exception {
        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(BigInteger.valueOf(123456))
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(
                        post(SUMMARISE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void testSummariseOrderEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(BigInteger.ONE)
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(
                        post(SUMMARISE_ORDER_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testPurchaseOrderEndpoint_loyaltyPointsApplied_returnsStatusOK() throws Exception {
        var books = transactionTemplate.execute(x -> List.of(
                createBook(BookType.REGULAR, "T5", "A5", 150D),
                createBook(BookType.NEW_RELEASE, "T6", "A6", 200D),
                createBook(BookType.OLD_EDITION, "T7", "A7", 100D)
        ));
        assertThat(books).isNotNull();

        setLoyaltyPointsToUser(AuthHeaderService.REGULAR_USER_USERNAME, maxLoyaltyPoints);

        var request = OrderRequestDto.builder()
                .order(
                        List.of(
                                BookQuantityRequestDto.builder()
                                        .bookId(books.get(0).getId())
                                        .quantity(3)
                                        .build(),
                                BookQuantityRequestDto.builder()
                                        .bookId(books.get(1).getId())
                                        .quantity(3)
                                        .build(),
                                BookQuantityRequestDto.builder()
                                        .bookId(books.get(2).getId())
                                        .quantity(2)
                                        .build()
                        )
                )
                .build();

        var priceToPay =
                // This is REGULAR book.
                // We take into consideration its 10%-deducted-price (it's in a bundle with 3 or more books) multiplied by the quantity.
                // One instance of the most expensive (REGULAR or OLD_EDITION) book in the order is free (loyalty points applicable). That is why we multiply by 2 instead of 3.
                books.get(0).getPrice() * 0.9 * 2 +
                        // This is NEW_RELEASE book.
                        // We take into consideration its full price multiplied by the quantity (3).
                        books.get(1).getPrice() * 3 +
                        // This is OLD_EDITION book.
                        // We take into consideration its 20%-deducted-price with additional 5% deduction (it's in a bundle with 3 or more books) multiplied by the quantity (2).
                        books.get(2).getPrice() * 0.75 * 2;

        var loyaltyPointsAfterPurchase = 7;

        mockMvc.perform(
                        post(PURCHASE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePaid").value(priceToPay))
                .andExpect(jsonPath("$.loyaltyPointsAfterPurchase").value(loyaltyPointsAfterPurchase))
                .andExpect(jsonPath("$.loyaltyPointsApplied").value(true))
                .andExpect(jsonPath("$.deductedBook.id").value(books.get(0).getId()))
                .andExpect(jsonPath("$.deductedBook.title").value(books.get(0).getTitle()))
                .andExpect(jsonPath("$.deductedBook.deductedPrice").value(books.get(0).getPrice() * 0.9))
                .andExpect(jsonPath("$.books.[0].id").value(books.get(0).getId()))
                .andExpect(jsonPath("$.books.[0].title").value(books.get(0).getTitle()))
                .andExpect(jsonPath("$.books.[0].originalPrice").value(books.get(0).getPrice()))
                .andExpect(jsonPath("$.books.[0].priceAfterDiscount").value(books.get(0).getPrice() * 0.9))
                .andExpect(jsonPath("$.books.[0].quantity").value(3))
                .andExpect(jsonPath("$.books.[1].id").value(books.get(1).getId()))
                .andExpect(jsonPath("$.books.[1].title").value(books.get(1).getTitle()))
                .andExpect(jsonPath("$.books.[1].originalPrice").value(books.get(1).getPrice()))
                .andExpect(jsonPath("$.books.[1].priceAfterDiscount").value(books.get(1).getPrice()))
                .andExpect(jsonPath("$.books.[1].quantity").value(3))
                .andExpect(jsonPath("$.books.[2].id").value(books.get(2).getId()))
                .andExpect(jsonPath("$.books.[2].title").value(books.get(2).getTitle()))
                .andExpect(jsonPath("$.books.[2].originalPrice").value(books.get(2).getPrice()))
                .andExpect(jsonPath("$.books.[2].priceAfterDiscount").value(books.get(2).getPrice() * 0.75))
                .andExpect(jsonPath("$.books.[2].quantity").value(2))
                .andExpect(jsonPath("$.books.[3]").doesNotExist());

        assertEquals(getUserLoyaltyPoints(AuthHeaderService.REGULAR_USER_USERNAME), loyaltyPointsAfterPurchase);
    }

    @Test
    void testPurchaseOrderEndpoint_loyaltyPointsNotApplied_returnsStatusOK() throws Exception {
        var book = transactionTemplate.execute(x -> createBook(BookType.OLD_EDITION, "T8", "A8", 150D));
        assertThat(book).isNotNull();

        setLoyaltyPointsToUser(AuthHeaderService.ADMIN_USER_USERNAME, 0);

        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(book.getId())
                                .quantity(2)
                                .build()
                ))
                .build();

        var priceToPay =
                // This is OLD_EDITION book.
                // We take into consideration its 20%-deducted-price multiplied by the quantity (2).
                book.getPrice() * 0.8 * 2;

        var loyaltyPointsAfterPurchase = 2;

        mockMvc.perform(
                        post(PURCHASE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePaid").value(priceToPay))
                .andExpect(jsonPath("$.loyaltyPointsAfterPurchase").value(loyaltyPointsAfterPurchase))
                .andExpect(jsonPath("$.loyaltyPointsApplied").value(false))
                .andExpect(jsonPath("$.deductedBook").doesNotExist())
                .andExpect(jsonPath("$.books.[0].id").value(book.getId()))
                .andExpect(jsonPath("$.books.[0].title").value(book.getTitle()))
                .andExpect(jsonPath("$.books.[0].originalPrice").value(book.getPrice()))
                .andExpect(jsonPath("$.books.[0].priceAfterDiscount").value(book.getPrice() * 0.8))
                .andExpect(jsonPath("$.books.[0].quantity").value(2))
                .andExpect(jsonPath("$.books.[1]").doesNotExist());

        assertEquals(getUserLoyaltyPoints(AuthHeaderService.ADMIN_USER_USERNAME), loyaltyPointsAfterPurchase);
    }

    @Test
    void testPurchaseOrderEndpoint_withMissingData_returnsStatusBadRequest() throws Exception {
        mockMvc.perform(
                        post(PURCHASE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(OrderRequestDto.builder().order(List.of()).build()))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPurchaseOrderEndpoint_withNegativeQuantity_returnsStatusBadRequest() throws Exception {
        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(BigInteger.ONE)
                                .quantity(-1)
                                .build()
                ))
                .build();

        mockMvc.perform(
                        post(PURCHASE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPurchaseOrderEndpoint_withNonexistentBookId_returnsStatusNotFound() throws Exception {
        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(BigInteger.valueOf(123456))
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(
                        post(PURCHASE_ORDER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void testPurchaseOrderEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        var request = OrderRequestDto.builder()
                .order(List.of(
                        BookQuantityRequestDto.builder()
                                .bookId(BigInteger.ONE)
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(
                        post(PURCHASE_ORDER_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
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

    private void setLoyaltyPointsToUser(String username, int loyaltyPoints) {
        transactionTemplate.executeWithoutResult(x -> {
            var user = userInfoRepository.findByUsername(username).orElseThrow();
            var userLoyaltyPoints = userLoyaltyPointsRepository.findByUserInfo(user)
                    .orElseGet(() -> {
                        var userPointsToCreate = new UserLoyaltyPoints();
                        userPointsToCreate.setUserInfo(user);
                        userPointsToCreate.setLoyaltyPoints(0);
                        return userPointsToCreate;
                    });
            userLoyaltyPoints.setLoyaltyPoints(loyaltyPoints);
            userLoyaltyPointsRepository.save(userLoyaltyPoints);
        });
    }

    private Integer getUserLoyaltyPoints(String username) {
        return transactionTemplate.execute(x -> userInfoRepository.findByUsername(username)
                .flatMap(userLoyaltyPointsRepository::findByUserInfo)
                .map(UserLoyaltyPoints::getLoyaltyPoints)
                .orElse(0));
    }
}
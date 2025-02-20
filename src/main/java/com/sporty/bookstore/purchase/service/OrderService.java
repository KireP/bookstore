package com.sporty.bookstore.purchase.service;

import com.sporty.bookstore.exception.BooksCannotBeOrderedException;
import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import com.sporty.bookstore.inventory.service.BookService;
import com.sporty.bookstore.purchase.dto.request.BookQuantityRequestDto;
import com.sporty.bookstore.purchase.dto.request.OrderRequestDto;
import com.sporty.bookstore.purchase.dto.response.OrderPaymentResponseDto;
import com.sporty.bookstore.purchase.dto.response.OrderPriceCalculationResponseDto;
import com.sporty.bookstore.purchase.service.processor.DiscountProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final List<DiscountProcessor> discountProcessors;
    private final BookService bookService;
    private final LoyaltyPointsService loyaltyPointsService;

    @Transactional(readOnly = true)
    public OrderPriceCalculationResponseDto summariseOrder(OrderRequestDto orderRequestDto) {
        var books = validateAndGetBooksFromOrder(orderRequestDto);
        var numberOfBooksInOrder = getNumberOfBooksInOrder(orderRequestDto);
        var idToBookPriceMap = calculateBooksPrices(books, numberOfBooksInOrder);

        var fullPrice = orderRequestDto.getOrder()
                .stream()
                .mapToDouble(bookQuantity -> {
                    double price = idToBookPriceMap.get(bookQuantity.getBookId());
                    return price * bookQuantity.getQuantity();
                })
                .sum();

        var loyaltyPointsApplicable = loyaltyPointsApplicable(books, numberOfBooksInOrder);
        if (loyaltyPointsApplicable) {
            var mostExpensiveRegularOrOldEditionBook = getMostExpensiveRegularOrOldEditionBook(books);
            if (mostExpensiveRegularOrOldEditionBook.isPresent()) {
                fullPrice -= idToBookPriceMap.get(mostExpensiveRegularOrOldEditionBook.get());
            }
        }

        return new OrderPriceCalculationResponseDto(fullPrice, loyaltyPointsApplicable);
    }

    @Transactional
    public OrderPaymentResponseDto purchaseOrder(OrderRequestDto orderRequestDto) {
        var summarisedOrder = summariseOrder(orderRequestDto);
        var numberOfBooksInOrder = getNumberOfBooksInOrder(orderRequestDto);

        int loyaltyPoints;
        if (summarisedOrder.getLoyaltyPointsToBeApplied()) {
            int loyaltyPointsBeforePurchase = loyaltyPointsService.getLoyaltyPointsForLoggedInUser().getPoints();
            loyaltyPoints = loyaltyPointsService
                    .applyLoyaltyPointsToLoggedInUser(
                            loyaltyPointsBeforePurchase + numberOfBooksInOrder - 1 - loyaltyPointsService.retrieveMaxLoyaltyPoints(),
                            false
                    )
                    .getPoints();
        } else {
            loyaltyPoints = loyaltyPointsService
                    .applyLoyaltyPointsToLoggedInUser(numberOfBooksInOrder, true)
                    .getPoints();
        }

        return new OrderPaymentResponseDto(
                summarisedOrder.getPriceToPay(),
                loyaltyPoints,
                summarisedOrder.getLoyaltyPointsToBeApplied()
        );
    }

    private Collection<Book> validateAndGetBooksFromOrder(OrderRequestDto orderRequestDto) {
        var bookIds = orderRequestDto.getOrder()
                .stream()
                .map(BookQuantityRequestDto::getBookId)
                .toList();
        var booksMap = bookService.getBooksByIds(bookIds)
                .stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        if (!booksMap.keySet().containsAll(bookIds)) {
            throw new BooksCannotBeOrderedException();
        }
        return booksMap.values();
    }

    private int getNumberOfBooksInOrder(OrderRequestDto orderRequestDto) {
        return orderRequestDto.getOrder()
                .stream()
                .mapToInt(BookQuantityRequestDto::getQuantity)
                .sum();
    }

    private Map<BigInteger, Double> calculateBooksPrices(Collection<Book> books, int numberOfBooksInOrder) {
        return books
                .stream()
                .collect(Collectors.toMap(
                        Book::getId,
                        book -> discountProcessors.stream()
                                .mapToDouble(discountProcessor -> discountProcessor.getPrice(
                                        book.getPrice(),
                                        book.getType(),
                                        numberOfBooksInOrder
                                ))
                                .sum()
                ));
    }

    private boolean loyaltyPointsApplicable(Collection<Book> books, int numberOfBooksInOrder) {
        var currentNumberOfLoyaltyPoints = loyaltyPointsService.getLoyaltyPointsForLoggedInUser().getPoints();
        var maxPointsReached = numberOfBooksInOrder - 1 + currentNumberOfLoyaltyPoints >= loyaltyPointsService.retrieveMaxLoyaltyPoints();
        if (!maxPointsReached) {
            return false;
        }
        return books
                .stream()
                .anyMatch(book -> book.getType() == BookType.REGULAR || book.getType() == BookType.OLD_EDITION);
    }

    private Optional<BigInteger> getMostExpensiveRegularOrOldEditionBook(Collection<Book> books) {
        return books
                .stream()
                .filter(book -> book.getType() == BookType.REGULAR || book.getType() == BookType.OLD_EDITION)
                .max(Comparator.comparingDouble(Book::getPrice))
                .map(Book::getId);
    }
}

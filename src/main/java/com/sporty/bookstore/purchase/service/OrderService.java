package com.sporty.bookstore.purchase.service;

import com.sporty.bookstore.exception.BooksCannotBeOrderedException;
import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import com.sporty.bookstore.inventory.service.BookService;
import com.sporty.bookstore.purchase.dto.request.BookQuantityRequestDto;
import com.sporty.bookstore.purchase.dto.request.OrderRequestDto;
import com.sporty.bookstore.purchase.dto.response.BookOrderDetailsResponseDto;
import com.sporty.bookstore.purchase.dto.response.DeductedBookResponseDto;
import com.sporty.bookstore.purchase.dto.response.OrderPaymentResponseDto;
import com.sporty.bookstore.purchase.dto.response.OrderPriceCalculationResponseDto;
import com.sporty.bookstore.purchase.service.processor.DiscountProcessorFactory;
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

    private final DiscountProcessorFactory discountProcessorFactory;
    private final BookService bookService;
    private final LoyaltyPointsService loyaltyPointsService;

    @Transactional(readOnly = true)
    public OrderPriceCalculationResponseDto summariseOrder(OrderRequestDto orderRequestDto) {
        var idToBookMap = validateAndGetBooksFromOrder(orderRequestDto);
        var books = idToBookMap.values();
        var numberOfBooksInOrder = getNumberOfBooksInOrder(orderRequestDto);
        var idToBookPriceMap = calculateBooksPrices(books, numberOfBooksInOrder);
        var fullPrice = calculateFullPrice(orderRequestDto, idToBookPriceMap);

        var result = OrderPriceCalculationResponseDto.builder();

        var loyaltyPointsApplicable = loyaltyPointsApplicable(books, numberOfBooksInOrder);
        result.loyaltyPointsToBeApplied(loyaltyPointsApplicable);

        if (loyaltyPointsApplicable) {
            var mostExpensiveRegularOrOldEditionBook = getMostExpensiveRegularOrOldEditionBook(books, numberOfBooksInOrder);
            if (mostExpensiveRegularOrOldEditionBook.isPresent()) {
                var freeBook = mostExpensiveRegularOrOldEditionBook.get();
                var freeBookPrice = idToBookPriceMap.get(freeBook.getId());
                fullPrice -= freeBookPrice;
                result.bookToBeDeducted(
                        new DeductedBookResponseDto(freeBook.getId(), freeBook.getTitle(), freeBookPrice)
                );
            }
        }

        return result.priceToPay(fullPrice)
                .books(getBookOrderDetails(orderRequestDto, idToBookMap, idToBookPriceMap))
                .build();
    }

    @Transactional
    public OrderPaymentResponseDto purchaseOrder(OrderRequestDto orderRequestDto) {
        var summarisedOrder = summariseOrder(orderRequestDto);
        var numberOfBooksInOrder = getNumberOfBooksInOrder(orderRequestDto);

        return new OrderPaymentResponseDto(
                summarisedOrder.getPriceToPay(),
                calculateLoyaltyPointsAfterPurchase(summarisedOrder.getLoyaltyPointsToBeApplied(), numberOfBooksInOrder),
                summarisedOrder.getLoyaltyPointsToBeApplied(),
                summarisedOrder.getBookToBeDeducted(),
                summarisedOrder.getBooks()
        );
    }

    private Map<BigInteger, Book> validateAndGetBooksFromOrder(OrderRequestDto orderRequestDto) {
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
        return booksMap;
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
                        book -> discountProcessorFactory
                                .get(book.getType().toString())
                                .getDiscountedPrice(book.getPrice(), numberOfBooksInOrder)

                ));
    }

    private double calculateFullPrice(OrderRequestDto orderRequestDto, Map<BigInteger, Double> idToBookPriceMap) {
        return orderRequestDto.getOrder()
                .stream()
                .mapToDouble(bookQuantity -> {
                    double price = idToBookPriceMap.get(bookQuantity.getBookId());
                    return price * bookQuantity.getQuantity();
                })
                .sum();
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

    private Optional<Book> getMostExpensiveRegularOrOldEditionBook(Collection<Book> books, int numberOfBooksInOrder) {
        return books
                .stream()
                .filter(book -> book.getType() == BookType.REGULAR || book.getType() == BookType.OLD_EDITION)
                .max(Comparator.comparingDouble(
                        book -> discountProcessorFactory
                                .get(book.getType().toString())
                                .getDiscountedPrice(book.getPrice(), numberOfBooksInOrder)
                ));
    }

    private List<BookOrderDetailsResponseDto> getBookOrderDetails(OrderRequestDto orderRequestDto,
                                                                  Map<BigInteger, Book> idToBookMap,
                                                                  Map<BigInteger, Double> idToBookPriceMap) {
        return orderRequestDto.getOrder()
                .stream()
                .map(bookQuantity -> {
                    var orderedBook = idToBookMap.get(bookQuantity.getBookId());
                    return new BookOrderDetailsResponseDto(
                            bookQuantity.getBookId(),
                            orderedBook.getTitle(),
                            orderedBook.getPrice(),
                            idToBookPriceMap.get(bookQuantity.getBookId()),
                            bookQuantity.getQuantity()
                    );
                })
                .toList();
    }

    private int calculateLoyaltyPointsAfterPurchase(boolean loyaltyPointsToBeApplied, int numberOfBooksInOrder) {
        int loyaltyPoints;
        if (loyaltyPointsToBeApplied) {
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
        return loyaltyPoints;
    }
}

package com.sporty.bookstore.purchase.service.processor;

import com.sporty.bookstore.inventory.enumeration.BookType;

public interface DiscountProcessor {

    default double getPrice(double originalPrice, BookType bookType, int numberOfBooksInBundle) {
        if (!shouldFire(bookType)) {
            return 0;
        }
        return getDiscountedPrice(originalPrice, numberOfBooksInBundle);
    }

    boolean shouldFire(BookType bookType);

    double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle);
}

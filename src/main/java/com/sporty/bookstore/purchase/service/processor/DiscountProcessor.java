package com.sporty.bookstore.purchase.service.processor;

public interface DiscountProcessor {

    double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle);
}

package com.sporty.bookstore.purchase.service.processor;

import org.springframework.stereotype.Service;

@Service("OLD_EDITION")
public class OldEditionDiscountProcessor implements DiscountProcessor {

    @Override
    public double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle) {
        if (numberOfBooksInBundle < 3) {
            return originalPrice * 0.8;
        }
        return originalPrice * 0.75;
    }
}

package com.sporty.bookstore.purchase.service.processor;

import org.springframework.stereotype.Service;

@Service("REGULAR")
public class RegularDiscountProcessor implements DiscountProcessor {

    @Override
    public double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle) {
        if (numberOfBooksInBundle < 3) {
            return originalPrice;
        }
        return originalPrice * 0.9;
    }
}

package com.sporty.bookstore.purchase.service.processor;

import org.springframework.stereotype.Service;

@Service("NEW_RELEASE")
public class NewReleaseDiscountProcessor implements DiscountProcessor {

    @Override
    public double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle) {
        return originalPrice;
    }
}

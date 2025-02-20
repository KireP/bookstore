package com.sporty.bookstore.purchase.service.processor;

import com.sporty.bookstore.inventory.enumeration.BookType;
import org.springframework.stereotype.Service;

@Service
public class OldEditionDiscountProcessor implements DiscountProcessor {

    @Override
    public boolean shouldFire(BookType bookType) {
        return bookType == BookType.OLD_EDITION;
    }

    @Override
    public double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle) {
        if (numberOfBooksInBundle < 3) {
            return originalPrice * 0.8;
        }
        return originalPrice * 0.75;
    }
}

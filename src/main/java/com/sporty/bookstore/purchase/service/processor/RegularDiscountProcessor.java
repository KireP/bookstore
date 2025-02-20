package com.sporty.bookstore.purchase.service.processor;

import com.sporty.bookstore.inventory.enumeration.BookType;
import org.springframework.stereotype.Service;

@Service
public class RegularDiscountProcessor implements DiscountProcessor {

    @Override
    public boolean shouldFire(BookType bookType) {
        return bookType == BookType.REGULAR;
    }

    @Override
    public double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle) {
        if (numberOfBooksInBundle < 3) {
            return originalPrice;
        }
        return originalPrice * 0.9;
    }
}

package com.sporty.bookstore.purchase.service.processor;

import com.sporty.bookstore.inventory.enumeration.BookType;
import org.springframework.stereotype.Service;

@Service
public class NewReleaseDiscountProcessor implements DiscountProcessor {

    @Override
    public boolean shouldFire(BookType bookType) {
        return bookType == BookType.NEW_RELEASE;
    }

    @Override
    public double getDiscountedPrice(double originalPrice, int numberOfBooksInBundle) {
        return originalPrice;
    }
}

package com.sporty.bookstore.purchase.service.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiscountProcessorFactory {

    private final Map<String, DiscountProcessor> discountProcessors;

    public DiscountProcessor get(String bookType) {
        var discountProcessor = discountProcessors.get(bookType);
        if (Objects.isNull(discountProcessor)) {
            throw new IllegalArgumentException("Unsupported book type");
        }
        return discountProcessor;
    }
}

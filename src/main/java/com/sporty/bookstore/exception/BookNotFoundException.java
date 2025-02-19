package com.sporty.bookstore.exception;

import java.math.BigInteger;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(BigInteger bookId) {
        super(String.format("Book with ID %d not found", bookId));
    }
}

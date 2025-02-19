package com.sporty.bookstore.exception;

import com.sporty.bookstore.inventory.enumeration.BookType;

import java.math.BigInteger;

public class BookCannotBeDeletedException extends RuntimeException {

    public BookCannotBeDeletedException(BigInteger bookId) {
        super(String.format("Book with ID %d cannot be deleted -- only books of type %s are eligible for deletion", bookId, BookType.OLD_EDITION));
    }
}

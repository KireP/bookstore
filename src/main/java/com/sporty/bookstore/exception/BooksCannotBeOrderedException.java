package com.sporty.bookstore.exception;

public class BooksCannotBeOrderedException extends RuntimeException {

    public BooksCannotBeOrderedException() {
        super("Not all ordered books are present in our inventory");
    }
}

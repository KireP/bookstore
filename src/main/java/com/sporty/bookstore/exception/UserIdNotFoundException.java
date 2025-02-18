package com.sporty.bookstore.exception;

import java.math.BigInteger;

public class UserIdNotFoundException extends RuntimeException {

    public UserIdNotFoundException(BigInteger userId) {
        super(String.format("User with ID %d not found", userId));
    }
}

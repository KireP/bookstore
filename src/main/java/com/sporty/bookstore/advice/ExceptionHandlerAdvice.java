package com.sporty.bookstore.advice;

import com.sporty.bookstore.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(exception = {
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            UsernameAlreadyExistsException.class,
            BookCannotBeDeletedException.class
    })
    public Map<String, String> handleBadRequestExceptions(Exception exception) {
        return getErrorMessage(exception);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(exception = {
            UserIdNotFoundException.class,
            BookNotFoundException.class,
            BooksCannotBeOrderedException.class
    })
    public Map<String, String> handleNotFoundExceptions(Exception exception) {
        return getErrorMessage(exception);
    }

    private Map<String, String> getErrorMessage(Exception exception) {
        return Map.of("message", exception.getMessage());
    }
}
package com.sporty.bookstore.advice;

import com.sporty.bookstore.exception.UserIdNotFoundException;
import com.sporty.bookstore.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return getErrorMessage(exception);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public Map<String, String> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException exception) {
        return getErrorMessage(exception);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserIdNotFoundException.class)
    public Map<String, String> handleUserIdNotFoundException(UserIdNotFoundException exception) {
        return getErrorMessage(exception);
    }

    private Map<String, String> getErrorMessage(Exception exception) {
        return Map.of("message", exception.getMessage());
    }
}
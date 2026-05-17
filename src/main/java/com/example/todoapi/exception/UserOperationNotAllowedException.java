package com.example.todoapi.exception;

public class UserOperationNotAllowedException extends RuntimeException {

    public UserOperationNotAllowedException(String message) {
        super(message);
    }
}

package com.example.todoapi.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }
}

package com.example.todoapi.vo;

import java.time.OffsetDateTime;
import java.util.List;

public class ApiErrorResponse {

    private String code;
    private String message;
    private OffsetDateTime timestamp;
    private List<String> errors;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String code, String message, OffsetDateTime timestamp, List<String> errors) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.errors = errors;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}

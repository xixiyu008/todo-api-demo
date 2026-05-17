package com.example.todoapi.vo;

import java.time.OffsetDateTime;

public class ApiResponse<T> {

    private String code;
    private String message;
    private OffsetDateTime timestamp;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String code, String message, OffsetDateTime timestamp, T data) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.data = data;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

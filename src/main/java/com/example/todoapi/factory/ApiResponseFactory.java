package com.example.todoapi.factory;

import com.example.todoapi.vo.ApiErrorResponse;
import com.example.todoapi.vo.ApiResponse;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class ApiResponseFactory {

    public <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, OffsetDateTime.now(), data);
    }

    public <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>("CREATED", message, OffsetDateTime.now(), data);
    }

    public ApiErrorResponse validationError(List<String> errors) {
        return new ApiErrorResponse("VALIDATION_ERROR", "Validation failed", OffsetDateTime.now(), errors);
    }

    public ApiErrorResponse todoNotFound(String message) {
        return new ApiErrorResponse("TODO_NOT_FOUND", message, OffsetDateTime.now(), List.of(message));
    }

    public ApiErrorResponse userNotFound(String message) {
        return new ApiErrorResponse("USER_NOT_FOUND", message, OffsetDateTime.now(), List.of(message));
    }

    public ApiErrorResponse unauthorized(String message) {
        return new ApiErrorResponse("UNAUTHORIZED", message, OffsetDateTime.now(), List.of(message));
    }

    public ApiErrorResponse badRequest(String code, String message) {
        return new ApiErrorResponse(code, message, OffsetDateTime.now(), List.of(message));
    }

    public ApiErrorResponse conflict(String message) {
        return new ApiErrorResponse("USER_ALREADY_EXISTS", message, OffsetDateTime.now(), List.of(message));
    }
}

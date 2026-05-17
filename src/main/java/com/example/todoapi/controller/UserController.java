package com.example.todoapi.controller;

import com.example.todoapi.factory.ApiResponseFactory;
import com.example.todoapi.mapper.UserResponseMapper;
import com.example.todoapi.model.AppUser;
import com.example.todoapi.service.AppUserService;
import com.example.todoapi.vo.ApiResponse;
import com.example.todoapi.vo.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserService appUserService;
    private final UserResponseMapper userResponseMapper;
    private final ApiResponseFactory apiResponseFactory;

    public UserController(
            AppUserService appUserService,
            UserResponseMapper userResponseMapper,
            ApiResponseFactory apiResponseFactory
    ) {
        this.appUserService = appUserService;
        this.userResponseMapper = userResponseMapper;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(Authentication authentication) {
        AppUser user = appUserService.findByUsername(authentication.getName());
        return apiResponseFactory.ok("Current user fetched successfully", userResponseMapper.toResponse(user));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        return apiResponseFactory.ok(
                "Users fetched successfully",
                userResponseMapper.toResponseList(appUserService.findAll())
        );
    }

    @PutMapping("/{id}/enable")
    public ApiResponse<UserResponse> enableUser(@PathVariable Long id, Authentication authentication) {
        AppUser user = appUserService.updateEnabledStatus(id, true, authentication.getName());
        return apiResponseFactory.ok("User enabled successfully", userResponseMapper.toResponse(user));
    }

    @PutMapping("/{id}/disable")
    public ApiResponse<UserResponse> disableUser(@PathVariable Long id, Authentication authentication) {
        AppUser user = appUserService.updateEnabledStatus(id, false, authentication.getName());
        return apiResponseFactory.ok("User disabled successfully", userResponseMapper.toResponse(user));
    }
}

package com.example.todoapi.controller;

import com.example.todoapi.dto.LoginRequest;
import com.example.todoapi.dto.RegisterUserRequest;
import com.example.todoapi.factory.ApiResponseFactory;
import com.example.todoapi.mapper.UserResponseMapper;
import com.example.todoapi.model.AppUser;
import com.example.todoapi.security.JwtService;
import com.example.todoapi.service.AppUserService;
import com.example.todoapi.vo.ApiResponse;
import com.example.todoapi.vo.LoginResponse;
import com.example.todoapi.vo.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApiResponseFactory apiResponseFactory;
    private final AppUserService appUserService;
    private final UserResponseMapper userResponseMapper;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            ApiResponseFactory apiResponseFactory,
            AppUserService appUserService,
            UserResponseMapper userResponseMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.apiResponseFactory = apiResponseFactory;
        this.appUserService = appUserService;
        this.userResponseMapper = userResponseMapper;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            return apiResponseFactory.ok("Login successful", new LoginResponse(token, "Bearer"));
        } catch (BadCredentialsException exception) {
            throw exception;
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        AppUser user = appUserService.register(request.getUsername(), request.getPassword());
        return apiResponseFactory.created("User registered successfully", userResponseMapper.toResponse(user));
    }
}

package com.example.todoapi.mapper;

import com.example.todoapi.model.AppUser;
import com.example.todoapi.vo.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserResponseMapper {

    public UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled());
    }

    public List<UserResponse> toResponseList(List<AppUser> users) {
        return users.stream().map(this::toResponse).toList();
    }
}

package com.example.todoapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.todoapi.exception.UserAlreadyExistsException;
import com.example.todoapi.exception.UserNotFoundException;
import com.example.todoapi.exception.UserOperationNotAllowedException;
import com.example.todoapi.mapper.AppUserMapper;
import com.example.todoapi.model.AppUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserMapper appUserMapper, PasswordEncoder passwordEncoder) {
        this.appUserMapper = appUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String username, String password) {
        String normalizedUsername = username.trim();
        if (existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException(normalizedUsername);
        }

        AppUser user = new AppUser(
                null,
                normalizedUsername,
                passwordEncoder.encode(password),
                "USER",
                true
        );
        appUserMapper.insert(user);
        return user;
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                .orderByAsc(AppUser::getId));
    }

    @Transactional(readOnly = true)
    public AppUser findByUsername(String username) {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, username)
                .last("LIMIT 1"));
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return user;
    }

    @Transactional
    public AppUser updateEnabledStatus(Long id, boolean enabled, String operatorUsername) {
        AppUser user = appUserMapper.selectById(id);
        if (user == null) {
            throw new UserNotFoundException(String.valueOf(id));
        }

        if (!enabled && user.getUsername().equals(operatorUsername)) {
            throw new UserOperationNotAllowedException("不能禁用当前登录的管理员账号");
        }

        user.setEnabled(enabled);
        appUserMapper.updateById(user);
        return user;
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        Long count = appUserMapper.selectCount(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, username));
        return count != null && count > 0;
    }
}

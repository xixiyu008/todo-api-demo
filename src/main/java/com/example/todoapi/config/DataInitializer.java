package com.example.todoapi.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.todoapi.mapper.AppUserMapper;
import com.example.todoapi.model.AppUser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDefaultAdmin(
            AuthProperties authProperties,
            AppUserMapper appUserMapper,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Long count = appUserMapper.selectCount(new LambdaQueryWrapper<AppUser>()
                    .eq(AppUser::getUsername, authProperties.getUsername()));
            if (count == null || count == 0) {
                AppUser admin = new AppUser(
                        null,
                        authProperties.getUsername(),
                        passwordEncoder.encode(authProperties.getPassword()),
                        "ADMIN",
                        true
                );
                appUserMapper.insert(admin);
            }
        };
    }
}

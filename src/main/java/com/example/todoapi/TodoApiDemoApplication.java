package com.example.todoapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.todoapi.mapper")
public class TodoApiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApiDemoApplication.class, args);
    }
}

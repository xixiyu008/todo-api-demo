package com.example.todoapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.todoapi.model.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
}

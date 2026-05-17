package com.example.todoapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.todoapi.model.AppUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}

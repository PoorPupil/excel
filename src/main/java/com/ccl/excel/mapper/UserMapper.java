package com.ccl.excel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccl.excel.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

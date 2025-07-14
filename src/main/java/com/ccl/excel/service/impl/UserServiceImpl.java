package com.ccl.excel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccl.excel.mapper.UserMapper;
import com.ccl.excel.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> {
}

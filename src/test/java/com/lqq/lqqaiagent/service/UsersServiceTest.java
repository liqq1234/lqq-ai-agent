package com.lqq.lqqaiagent.service;

import com.lqq.lqqaiagent.model.entity.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
@SpringBootTest
class UsersServiceTest {

    @Resource
    UserService userService;
    @Test
    void testAddUser(){
        User user = new User();
        user.setUsername("lqq");
        user.setPassword("123456");
        user.setEmail("3477981312@qq.com");
        user.setPhone("15865799671");
        user.setRole(0);
        user.setStatus(0);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setLastLogin(new Date());
        boolean result = userService.save(user);
        System.out.println(result);
    }

    @Test
    void userRegister() {
        String email = "qingquanli325@gmail.com";
        String username = "小泉";
        String password = "12345678";
        String checkPassword = "12345678";

        long userId = userService.userRegister(email, username,password, checkPassword);

        Assertions.assertTrue(userId > 0);


    }
}
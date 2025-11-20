package com.lqq.lqqaiagent.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lqq.lqqaiagent")
@MapperScan("com.lqq.lqqaiagent.mapper")
public class LqqChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(LqqChatApplication.class, args);
    }
}

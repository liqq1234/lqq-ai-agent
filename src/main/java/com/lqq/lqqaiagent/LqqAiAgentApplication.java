package com.lqq.lqqaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lqq.lqqaiagent.mapper")

public class LqqAiAgentApplication {
    public static void main(String[] args) {
		SpringApplication.run(LqqAiAgentApplication.class, args);
	}

}

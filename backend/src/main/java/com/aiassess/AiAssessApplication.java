package com.aiassess;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aiassess.mapper")
public class AiAssessApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiAssessApplication.class, args);
    }
}

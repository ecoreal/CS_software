package com.bengebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bengebackend.mapper")
public class BengeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BengeBackendApplication.class, args);
    }

}

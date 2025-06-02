package com.ohgiraffers.refrigegobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RefrigeGoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefrigeGoBackendApplication.class, args);
    }

}

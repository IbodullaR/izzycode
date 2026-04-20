package com.code.algonix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlgonixApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlgonixApplication.class, args);
    }
}

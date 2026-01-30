package com.devtoolmp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevToolMpApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevToolMpApplication.class, args);
    }
}

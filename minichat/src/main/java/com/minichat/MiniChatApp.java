package com.minichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiniChatApp {
    public static void main(String[] args) {
        SpringApplication.run(MiniChatApp.class, args);
    }
}

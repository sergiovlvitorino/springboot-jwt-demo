package com.sergiovitorino.springbootjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }

}

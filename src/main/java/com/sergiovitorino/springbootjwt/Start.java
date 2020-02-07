package com.sergiovitorino.springbootjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class Start {
	public static void main(String[] args) {
		SpringApplication.run(Start.class, args);
	}
}

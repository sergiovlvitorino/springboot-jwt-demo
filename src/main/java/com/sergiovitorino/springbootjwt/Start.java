package com.sergiovitorino.springbootjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableAspectJAutoProxy
@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Start {

	public static void main(String[] args) {
		SpringApplication.run(Start.class, args);
	}

}

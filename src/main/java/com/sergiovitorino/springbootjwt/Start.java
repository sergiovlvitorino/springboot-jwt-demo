package com.sergiovitorino.springbootjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

<<<<<<< HEAD
@EnableAspectJAutoProxy
@SpringBootApplication
=======
@EnableWebMvc
>>>>>>> b53c957fa28e113df9141914607c146cd922224b
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class Start {
	public static void main(String[] args) {
		SpringApplication.run(Start.class, args);
	}
}

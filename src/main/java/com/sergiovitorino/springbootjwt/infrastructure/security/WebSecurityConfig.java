package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private JWTAuthenticationFilter jwtAuthenticationFilter;
	@Autowired private TokenAuthenticationService tokenAuthenticationService;
	@Autowired private UserDetailsService userDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		JWTLoginFilter jwtLoginFilter = new JWTLoginFilter("/login", authenticationManager(), tokenAuthenticationService);
		
		http.cors().and().csrf().disable()
			.authorizeRequests()
				//.antMatchers("/**").permitAll()
				.antMatchers(HttpMethod.GET, "/user/count").permitAll()
				.antMatchers(HttpMethod.GET, "/user").permitAll()
				.antMatchers(HttpMethod.POST, "/passwordReset").permitAll()
				.antMatchers(HttpMethod.PUT, "/passwordReset").permitAll()
				.antMatchers(HttpMethod.POST, "/login").permitAll()
				.antMatchers( "/socket/**").permitAll()
				.antMatchers("/actuator/**").permitAll()

				
				.anyRequest().authenticated().and()
					// We filter the api/login requests
					.addFilterBefore(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)

					// And filter other requests to check the presence of JWT in header
					.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);		
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

}

package com.sergiovitorino.springbootjwt.infrastructure.util;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class LoginHelper {

	public HttpHeaders createAuthenticatedHeader(TestRestTemplate restTemplete, Integer port) {
		return createAuthenticatedHeader(restTemplete, port, MediaType.APPLICATION_JSON_UTF8_VALUE );
	}

	public HttpHeaders createAuthenticatedHeader(TestRestTemplate restTemplete, Integer port, String mediaType) {
		return createAuthenticatedHeader(restTemplete, port, "abc@def.com", "123456", mediaType );
	}

	public HttpHeaders createAuthenticatedHeader(TestRestTemplate restTemplete, Integer port, String username, String password, String mediaType) {
		String credencials = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_UTF8_VALUE));
		HttpEntity<String> entity = new HttpEntity<String>(credencials, headers);
		ResponseEntity<String> responseEntity = restTemplete.exchange("http://localhost:" + port + "/login", HttpMethod.POST, entity, String.class);
		String token = responseEntity.getHeaders().getFirst("Authorization");
		headers.add("Authorization", token);
		headers.setContentType(MediaType.valueOf(mediaType));
		return headers;
	}
	
}

package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private Integer port;

    @Test
    public void testIfHttpStatusIsForbiddenWhenLoginIsWrong() {
        var username = UUID.randomUUID().toString();
        var password = UUID.randomUUID().toString();
        var credentials = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        var entity = new HttpEntity<>(credentials, headers);
        var responseEntity = restTemplate.exchange("http://localhost:" + port + "/login", HttpMethod.POST, entity, String.class);
        var statusCode = responseEntity.getStatusCode();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), statusCode.value());
    }

    @Test
    public void testIfHttpStatusIsOkWhenLoginIsAllRight() {
        String username = "abc@def.com";
        String password = "Test@1234";
        String credentials = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        HttpEntity<String> entity = new HttpEntity<>(credentials, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:" + port + "/login", HttpMethod.POST, entity, String.class);
        var statusCode = responseEntity.getStatusCode();
        assertEquals(HttpStatus.OK.value(), statusCode.value());
    }

    @Test
    public void testIfHttpStatusReturnsForbiddenWhenTokenIsEmpty() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        headers.add("Authorization", "");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.GET, entity, String.class);
        var statusCode = responseEntity.getStatusCode();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), statusCode.value());
    }

}
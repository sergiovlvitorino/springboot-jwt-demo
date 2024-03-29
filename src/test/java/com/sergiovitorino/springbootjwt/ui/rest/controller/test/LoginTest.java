package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private TestRestTemplate restTemplete;
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
        var responseEntity = restTemplete.exchange("http://localhost:" + port + "/login", HttpMethod.POST, entity, String.class);
        var statusCode = responseEntity.getStatusCode();
        assertEquals(HttpStatus.FORBIDDEN, statusCode);
    }

    @Test
    public void testIfHttpStatusIsOkWhenLoginIsAllRight() {
        String username = "abc@def.com";
        String password = "123456";
        String credentials = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        HttpEntity<String> entity = new HttpEntity<>(credentials, headers);
        ResponseEntity<String> responseEntity = restTemplete.exchange("http://localhost:" + port + "/login", HttpMethod.POST, entity, String.class);
        HttpStatus statusCode = responseEntity.getStatusCode();
        assertEquals(HttpStatus.OK, statusCode);
    }

    @Test
    public void testIfHttpStatusReturnsForbiddenWhenTokenIsEmpty() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        headers.add("Authorization", "");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> responseEntity = restTemplete.exchange("http://localhost:" + port + "/user", HttpMethod.GET, entity, String.class);
        HttpStatus statusCode = responseEntity.getStatusCode();
        assertEquals(HttpStatus.FORBIDDEN, statusCode);
    }

}
package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private Integer port;
    @Autowired
    private ApplicationContext applicationContext;

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

    /**
     * T16 — Conta bloqueada não deve conseguir autenticar.
     *
     * Cria um usuário com accountLocked=true diretamente via repository (sem passar
     * pelo UserService, que sempre força accountLocked=false) e valida que a tentativa
     * de login retorna 401 Unauthorized, pois Spring Security lança LockedException
     * quando isAccountNonLocked() retorna false.
     */
    @Test
    public void testIfLoginReturns401WhenAccountIsLocked() {
        // Arrange — cria usuário com conta bloqueada diretamente via repository
        // Beans obtained programmatically to avoid changing the Spring context cache key
        var userRepository = applicationContext.getBean(UserRepository.class);
        var roleRepository = applicationContext.getBean(RoleRepository.class);
        var passwordEncoder = applicationContext.getBean(PasswordEncoder.class);

        var role = roleRepository.findAll().get(0);
        var rawPassword = "Test@1234";
        var lockedEmail = "locked-" + UUID.randomUUID() + "@test.com";

        var lockedUser = new User();
        lockedUser.setName("Locked User");
        lockedUser.setEmail(lockedEmail);
        lockedUser.setPassword(passwordEncoder.encode(rawPassword));
        lockedUser.setEnabled(true);
        lockedUser.setAccountLocked(true);
        lockedUser.setRole(role);
        userRepository.saveAndFlush(lockedUser);

        var credentials = "{\"username\":\"" + lockedEmail + "\",\"password\":\"" + rawPassword + "\"}";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(credentials, headers);

        // Act
        var responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/login",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Assert — conta bloqueada deve resultar em 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getStatusCode().value());
    }

}
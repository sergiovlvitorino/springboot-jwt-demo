package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.model.RefreshToken;
import com.sergiovitorino.springbootjwt.domain.repository.RefreshTokenRepository;
import com.sergiovitorino.springbootjwt.infrastructure.security.RefreshTokenHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RefreshTokenIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @LocalServerPort
    private Integer port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private ResponseEntity<String> doLogin(String username, String password) {
        String credentials = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(credentials, headers);
        return restTemplate.exchange(baseUrl() + "/login", HttpMethod.POST, entity, String.class);
    }

    @Test
    void login_returnsRefreshTokenInBody() throws Exception {
        ResponseEntity<String> response = doLogin("abc@def.com", "Test@1234");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.has("accessToken")).isTrue();
        assertThat(body.has("refreshToken")).isTrue();
        assertThat(body.get("accessToken").asText()).isNotBlank();
        assertThat(body.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void postAuthRefresh_withValidToken_returnsNewTokens() throws Exception {
        ResponseEntity<String> loginResponse = doLogin("abc@def.com", "Test@1234");
        JsonNode loginBody = objectMapper.readTree(loginResponse.getBody());
        String refreshToken = loginBody.get("refreshToken").asText();

        String requestBody = "{\"refreshToken\":\"" + refreshToken + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/auth/refresh", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("accessToken").asText()).isNotBlank();
        assertThat(body.get("refreshToken").asText()).isNotBlank();
        assertThat(body.get("refreshToken").asText()).isNotEqualTo(refreshToken);
    }

    @Test
    void postAuthRefresh_withUsedToken_returns401() throws Exception {
        ResponseEntity<String> loginResponse = doLogin("abc@def.com", "Test@1234");
        JsonNode loginBody = objectMapper.readTree(loginResponse.getBody());
        String refreshToken = loginBody.get("refreshToken").asText();

        String requestBody = "{\"refreshToken\":\"" + refreshToken + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(baseUrl() + "/auth/refresh", HttpMethod.POST, entity, String.class);
        ResponseEntity<String> secondResponse = restTemplate.exchange(
                baseUrl() + "/auth/refresh", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), secondResponse.getStatusCode().value());
    }

    @Test
    void postAuthRefresh_withExpiredToken_returns401() throws Exception {
        UUID userId = refreshTokenRepository.findAll().stream()
                .findFirst()
                .map(RefreshToken::getUserId)
                .orElseGet(() -> {
                    doLogin("abc@def.com", "Test@1234");
                    return refreshTokenRepository.findAll().stream()
                            .findFirst()
                            .map(RefreshToken::getUserId)
                            .orElse(UUID.randomUUID());
                });

        String rawExpiredToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawExpiredToken);
        RefreshToken expiredToken = new RefreshToken(tokenHash, userId, LocalDateTime.now().minusDays(1));
        refreshTokenRepository.save(expiredToken);

        String requestBody = "{\"refreshToken\":\"" + rawExpiredToken + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/auth/refresh", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }

    @Test
    void postAuthRefresh_withInvalidToken_returns401() {
        String requestBody = "{\"refreshToken\":\"" + RefreshTokenHasher.generateToken() + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/auth/refresh", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }
}

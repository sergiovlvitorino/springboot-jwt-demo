package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityHeadersTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    @Test
    void shouldReturnStrictTransportSecurityHeader() {
        var response = doLoginRequest();

        var hsts = response.getHeaders().getFirst("Strict-Transport-Security");
        assertThat(hsts)
                .as("HSTS header deve estar presente")
                .isNotNull()
                .contains("max-age=31536000")
                .contains("includeSubDomains");
    }

    @Test
    void shouldReturnContentSecurityPolicyHeader() {
        var response = doLoginRequest();

        var csp = response.getHeaders().getFirst("Content-Security-Policy");
        assertThat(csp)
                .as("CSP header deve estar presente")
                .isNotNull()
                .isEqualTo("default-src 'none'");
    }

    @Test
    void shouldReturnReferrerPolicyHeader() {
        var response = doLoginRequest();

        var referrerPolicy = response.getHeaders().getFirst("Referrer-Policy");
        assertThat(referrerPolicy)
                .as("Referrer-Policy header deve estar presente")
                .isNotNull()
                .isEqualTo("strict-origin-when-cross-origin");
    }

    @Test
    void shouldReturnXContentTypeOptionsHeader() {
        var response = doLoginRequest();

        var xContentTypeOptions = response.getHeaders().getFirst("X-Content-Type-Options");
        assertThat(xContentTypeOptions)
                .as("X-Content-Type-Options header deve estar presente")
                .isNotNull()
                .isEqualTo("nosniff");
    }

    @Test
    void shouldReturnXFrameOptionsDenyHeader() {
        var response = doLoginRequest();

        var xFrameOptions = response.getHeaders().getFirst("X-Frame-Options");
        assertThat(xFrameOptions)
                .as("X-Frame-Options header deve estar presente")
                .isNotNull()
                .isEqualTo("DENY");
    }

    private ResponseEntity<String> doLoginRequest() {
        var credentials = "{\"username\":\"abc@def.com\",\"password\":\"Test@1234\"}";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(credentials, headers);
        return restTemplate.exchange(
                "http://localhost:" + port + "/login",
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}

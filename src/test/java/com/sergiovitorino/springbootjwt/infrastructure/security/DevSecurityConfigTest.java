package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.infrastructure.Initialize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida que DevSecurityConfig e carregado no profile dev e que sua cadeia
 * de seguranca cobre /h2-console/** com securityMatcher dedicado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilterChainProxy filterChainProxy;

    @Autowired
    private List<SecurityFilterChain> filterChains;

    // Evita que Initialize tente popular o banco durante o teste
    @MockitoBean
    private Initialize initialize;

    @Test
    void devSecurityConfig_shouldBeLoaded() {
        // Verifica que ha pelo menos 2 cadeias de seguranca no profile dev
        // (h2ConsoleFilterChain + filterChain principal)
        assertTrue(filterChains.size() >= 2,
                "Profile dev deve ter pelo menos 2 filter chains (h2Console + principal)");
    }

    @Test
    void h2ConsoleFilterChain_shouldExistWithCorrectMatcher() {
        // Verifica que existe uma cadeia dedicada para /h2-console/**
        boolean hasH2Chain = filterChains.stream()
                .anyMatch(chain -> chain.toString().contains("h2-console")
                        || chain.toString().contains("h2Console"));

        // Alternativa: verifica via FilterChainProxy que /h2-console/ usa cadeia diferente de /rest/
        var h2Request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/h2-console/");
        h2Request.setServletPath("/h2-console/");

        var restRequest = new org.springframework.mock.web.MockHttpServletRequest("GET", "/rest/user");
        restRequest.setServletPath("/rest/user");

        var h2Chain = filterChainProxy.getFilterChains().stream()
                .filter(chain -> chain.matches(h2Request))
                .findFirst();

        var restChain = filterChainProxy.getFilterChains().stream()
                .filter(chain -> chain.matches(restRequest))
                .findFirst();

        assertTrue(h2Chain.isPresent(), "Deve haver uma cadeia que cobre /h2-console/");
        assertTrue(restChain.isPresent(), "Deve haver uma cadeia que cobre /rest/user");

        // As cadeias devem ser diferentes (DevSecurityConfig para h2, WebSecurityConfig para rest)
        assertNotSame(h2Chain.get(), restChain.get(),
                "h2-console e /rest devem usar cadeias de seguranca diferentes");
    }

    @Test
    void protectedEndpoint_shouldRequireAuth() throws Exception {
        // GET /rest/user sem token -> 401 (cadeia principal de seguranca)
        mockMvc.perform(get("/rest/user"))
                .andExpect(status().isUnauthorized());
    }
}

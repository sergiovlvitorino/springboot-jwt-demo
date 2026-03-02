package com.sergiovitorino.springbootjwt.contract;

import com.sergiovitorino.springbootjwt.application.service.UserService;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import com.sergiovitorino.springbootjwt.infrastructure.Initialize;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private Initialize initialize;

    @BeforeEach
    public void setup() {
        User mockUser = new User();
        mockUser.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        mockUser.setName("John Doe");
        mockUser.setEmail("john@example.com");

        when(userService.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), any(User.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockUser)));

        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}

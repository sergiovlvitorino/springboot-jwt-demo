package com.sergiovitorino.springbootjwt.application.service;


import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import com.sergiovitorino.springbootjwt.infrastructure.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class UserServiceTest {

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIfLoadByUsernameReturnsNullWhenNotFound() {
        var emailExpected = UUID.randomUUID().toString() + "@def.com";
        var repository = mock(UserRepository.class);
        when(repository.findByEmail(emailExpected)).thenReturn(null);
        var service = new UserService();
        service.setRepository(repository);
        service.setValidator(new Validator());

        var result = service.loadUserByUsername(emailExpected);
        assertNull(result);
    }

}

package com.sergiovitorino.springbootjwt.application.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import com.sergiovitorino.springbootjwt.infrastructure.Validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Before
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
        Assert.assertNull(result);
    }

}

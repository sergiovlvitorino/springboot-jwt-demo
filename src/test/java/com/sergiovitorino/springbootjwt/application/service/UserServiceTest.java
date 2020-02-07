package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import com.sergiovitorino.springbootjwt.infrastructure.Validator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIfLoadByUsernameReturnsNullWhenNotFound(){
        String emailExpected = UUID.randomUUID().toString() + "@def.com";
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByEmail(emailExpected)).thenReturn(null);
        UserService service = new UserService();
        service.setRepository(repository);
        service.setValidator(new Validator());
        UserDetails result = service.loadUserByUsername(emailExpected);
        Assert.assertNull(result);
    }

}

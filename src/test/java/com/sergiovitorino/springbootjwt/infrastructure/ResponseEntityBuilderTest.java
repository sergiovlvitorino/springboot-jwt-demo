package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ResponseEntityBuilderTest {

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIfResultChangesJsonProcessingExceptionToIllegalArgumentException() throws Exception {
        var responseEntityBuilder = new ResponseEntityBuilder();
        var validator = new Validator();
        responseEntityBuilder.setValidator(validator);

        var mapper = mock(ObjectMapper.class);
        var mockJsonProcessingException = mock(JsonProcessingException.class);
        lenient().when(mapper.writeValueAsString(any())).thenThrow(mockJsonProcessingException);
        responseEntityBuilder.setMapper(mapper);

        try {
            validator.addError("Mock Error");
            responseEntityBuilder.bindingResult(null).httpStatusError(HttpStatus.CONFLICT).result(null).build();
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

}

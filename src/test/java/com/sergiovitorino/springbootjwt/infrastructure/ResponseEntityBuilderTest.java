package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ResponseEntityBuilderTest {

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIfResultChangesJsonProcessingExceptionToIllegalArgumentException() throws Exception{
        ResponseEntityBuilder responseEntityBuilder = new ResponseEntityBuilder();
        Validator validator = new Validator();
        responseEntityBuilder.setValidator(validator);

        ObjectMapper mapper = mock(ObjectMapper.class);
        JsonProcessingException mockJsonProcessingException = mock(JsonProcessingException.class);
        lenient().when(mapper.writeValueAsString(any())).thenThrow(mockJsonProcessingException);
        responseEntityBuilder.setMapper(mapper);

        try{
            validator.addError("Mock Error");
            responseEntityBuilder.bindingResult(null).httpStatusError(HttpStatus.CONFLICT).result(null).build();
            Assert.fail();
        }catch (IllegalArgumentException e){
            Assert.assertTrue(true);
        }
    }

}

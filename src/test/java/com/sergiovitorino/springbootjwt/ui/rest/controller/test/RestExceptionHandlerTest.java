package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.ui.rest.controller.RestExceptionHandler;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
public class RestExceptionHandlerTest {

    @Test
    public void testIfExceptionParserIsOk() throws Exception {
        RestExceptionHandler restExceptionHandler = new RestExceptionHandler();
        restExceptionHandler.setMapper(new ObjectMapper());
        ResponseEntity responseEntity = restExceptionHandler.exceptionHandler(new IllegalArgumentException("Mock Exception"));
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

}

package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.ui.rest.controller.RestExceptionHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest
public class RestExceptionHandlerTest {

    @Test
    public void testIfExceptionParserIsOk() throws Exception {
        var restExceptionHandler = new RestExceptionHandler();
        restExceptionHandler.setMapper(new ObjectMapper());
        var responseEntity = restExceptionHandler.exceptionHandler(new IllegalArgumentException("Mock Exception"));
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

}

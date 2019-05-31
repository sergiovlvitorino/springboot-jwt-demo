package com.sergiovitorino.springbootjwt.infrastructure.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.infrastructure.ResponseBuilder;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ResponseBuilderTest {

    @Test
    public void testIfBuildCatchJsonProcessingException() throws Exception {
        String expectedMessage = "Cannot convert object to json";
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new JsonProcessingException(expectedMessage){});
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.setMapper(mapper);
        responseBuilder.load(new User());
        ResponseEntity<String> responseEntity = (ResponseEntity<String>) responseBuilder.build();
        JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        String actualMessage = jsonObject.getString("exception");
        assertEquals(expectedMessage, actualMessage);
    }

}

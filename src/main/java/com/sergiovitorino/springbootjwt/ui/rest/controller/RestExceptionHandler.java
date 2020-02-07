package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class RestExceptionHandler {

    private Logger log = Logger.getLogger(this.getClass().getName());
    @Autowired private ObjectMapper mapper;

    @ExceptionHandler
    public ResponseEntity exceptionHandler(final Exception exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        final ErrorBean errorBean = ErrorBean.builder().className(exception.getClass().getName()).fieldError("").message(exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapper.writeValueAsString(errorBean));
    }

    public void setMapper(ObjectMapper mapper){ this.mapper = mapper; }

}
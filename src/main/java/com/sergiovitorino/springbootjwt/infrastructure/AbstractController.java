package com.sergiovitorino.springbootjwt.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractController {
    
    private Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired protected ResponseBuilder responseBuilder;

    @ExceptionHandler
    public ResponseEntity exceptionHandler(Exception exception){
        log.log(Level.SEVERE, exception.getMessage());
        return responseBuilder.load(exception).build();
    }

}

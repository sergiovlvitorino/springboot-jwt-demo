package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractController {
    
    private Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired private ObjectMapper mapper;

    @ExceptionHandler(BindException.class)
    public ResponseEntity methodArgumentNotValidExceptionHandler(BindException exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        final List<ErrorBean> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().stream().forEach(fieldError -> {
            errors.add(new ErrorBean(exception.getClass().getName(), fieldError.getField(), fieldError.getDefaultMessage()));
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        final List<ErrorBean> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().stream().forEach(fieldError -> {
            errors.add(new ErrorBean(exception.getClass().getName(), fieldError.getField(), fieldError.getDefaultMessage()));
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity exceptionHandler(Exception exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        ErrorBean errorBean = new ErrorBean(exception.getClass().getName(), "", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapper.writeValueAsString(errorBean));
    }

}

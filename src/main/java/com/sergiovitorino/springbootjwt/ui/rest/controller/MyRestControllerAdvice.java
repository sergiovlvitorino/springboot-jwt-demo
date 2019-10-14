<<<<<<< HEAD:src/main/java/com/sergiovitorino/springbootjwt/ui/rest/controller/RestExceptionHandler.java
package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class RestExceptionHandler {

    private Logger log = Logger.getLogger(this.getClass().getName());
    @Autowired private ObjectMapper mapper;

    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity bindExceptionHandler(final BindException exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        final List<ErrorBean> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().stream().forEach(fieldError -> {
            final ErrorBean errorBean = ErrorBean.builder().className(exception.getClass().getName()).fieldError(fieldError.getField()).message(fieldError.getDefaultMessage()).build();
            errors.add(errorBean);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler
    public ResponseEntity exceptionHandler(final Exception exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        final ErrorBean errorBean = ErrorBean.builder().className(exception.getClass().getName()).fieldError("").message(exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapper.writeValueAsString(errorBean));
    }

=======
package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MyRestControllerAdvice {

    private Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    ObjectMapper mapper;

    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity bindExceptionHandler(final BindException exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        final List<ErrorBean> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().stream().forEach(fieldError -> {
            final ErrorBean errorBean =
                    ErrorBean.builder()
                            .className(exception.getClass().getName())
                            .fieldError(fieldError.getField())
                            .message(fieldError.getDefaultMessage())
                            .build();
            errors.add(errorBean);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exceptionHandler(final Exception exception) throws Exception {
        log.log(Level.SEVERE, exception.getMessage());
        final ErrorBean errorBean = ErrorBean.builder().className(exception.getClass().getName()).fieldError("").message(exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapper.writeValueAsString(errorBean));
    }

>>>>>>> b53c957fa28e113df9141914607c146cd922224b:src/main/java/com/sergiovitorino/springbootjwt/ui/rest/controller/MyRestControllerAdvice.java
}
package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
public class ResponseEntityBuilder {

    @Autowired private Validator validator;
    @Autowired private ObjectMapper mapper;

    private Object result;
    private BindingResult bindingResult;
    private HttpStatus httpStatus;
    private HttpStatus httpStatusError;

    public ResponseEntity build(){
        try{
            httpStatus = httpStatus == null ? HttpStatus.OK : httpStatus;

            if (bindingResult != null)
                return ResponseEntity.badRequest().body(mapper.writeValueAsString(parse(bindingResult)));

            if (validator.isInvalid())
                return ResponseEntity.status(httpStatusError).body(mapper.writeValueAsString(validator.getErrors()));

            return ResponseEntity.status(httpStatus).body(mapper.writeValueAsString(result));
        } catch (JsonProcessingException e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private List<ErrorBean> parse(BindingResult bindingResult) {
        final List<ErrorBean> errors = new ArrayList<>();
        bindingResult.getFieldErrors().stream().forEach(fieldError -> errors.add(ErrorBean.builder().fieldError(fieldError.getField()).message(fieldError.getDefaultMessage()).build()));
        return errors;
    }

    public ResponseEntityBuilder httpStatus(HttpStatus httpStatus){
        this.httpStatus = httpStatus;
        return this;
    }

    public ResponseEntityBuilder httpStatusError(HttpStatus httpStatusError){
        this.httpStatusError = httpStatusError;
        return this;
    }

    public ResponseEntityBuilder result(Object result) {
        this.result = result;
        return this;
    }

    public ResponseEntityBuilder bindingResult(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
        return this;
    }

    public void setMapper(ObjectMapper mapper) { this.mapper = mapper; }
    public void setValidator(Validator validator) { this.validator = validator; }

}

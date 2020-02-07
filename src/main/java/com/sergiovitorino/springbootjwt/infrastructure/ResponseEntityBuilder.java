package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class ResponseEntityBuilder {

    @Autowired private Validator validator;
    @Autowired private ObjectMapper mapper;
    private Object result;
    private BindingResult bindingResult;

    public ResponseEntity build(){
        try{
            validator.parse(bindingResult);
            if (validator.isValid())
                return ResponseEntity.ok(mapper.writeValueAsString(result));
            return ResponseEntity.badRequest().body(mapper.writeValueAsString(validator.getErrors()));
        } catch (JsonProcessingException e){
            throw new IllegalArgumentException(e.getMessage());
        }
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

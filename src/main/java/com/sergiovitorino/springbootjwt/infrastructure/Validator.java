package com.sergiovitorino.springbootjwt.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
public class Validator {

    private List<ErrorBean> errors;

    public Validator() {
        errors = new ArrayList<>();
    }

    public void addError(String message) {
        errors.add(ErrorBean.builder().message(message).build());
    }

    public void parse(BindingResult bindingResult) {
        if (bindingResult != null && bindingResult.hasErrors())
            bindingResult.getFieldErrors().stream().forEach(fieldError -> errors.add(ErrorBean.builder().fieldError(fieldError.getField()).message(fieldError.getDefaultMessage()).build()));
    }

    public List<ErrorBean> getErrors() {
        return this.errors;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }


}

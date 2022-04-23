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

    public List<ErrorBean> getErrors() {
        return this.errors;
    }

    public boolean isInvalid() {
        return !errors.isEmpty();
    }


}

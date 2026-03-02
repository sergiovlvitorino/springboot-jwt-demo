package com.sergiovitorino.springbootjwt.infrastructure;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Validator {

    private final List<ErrorBean> errors;

    public Validator() {
        errors = new ArrayList<>();
    }

    public void addError(String message) {
        errors.add(new ErrorBean(null, null, message));
    }

    public boolean isInvalid() {
        return !errors.isEmpty();
    }
    
    public void clearErrors() {
        this.errors.clear();
    }

    // Getter
    public List<ErrorBean> getErrors() {
        return errors;
    }

}

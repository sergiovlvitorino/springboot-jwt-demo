package com.sergiovitorino.springbootjwt.infrastructure.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");

    @Override
    public void initialize(SafeHtml constraintAnnotation) {
    }

    @Override
    public boolean isValid(String html, ConstraintValidatorContext constraintValidatorContext) {
        return html == null || !HTML_PATTERN.matcher(html).find();
    }

}

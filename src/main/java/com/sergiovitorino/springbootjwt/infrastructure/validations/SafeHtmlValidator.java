package com.sergiovitorino.springbootjwt.infrastructure.validations;

import org.jsoup.Jsoup;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {

    @Override
    public void initialize(SafeHtml constraintAnnotation) {
    }

    @Override
    public boolean isValid(String html, ConstraintValidatorContext constraintValidatorContext) {
        return html == null || Jsoup.parse(html).text().equals(html);
    }

}

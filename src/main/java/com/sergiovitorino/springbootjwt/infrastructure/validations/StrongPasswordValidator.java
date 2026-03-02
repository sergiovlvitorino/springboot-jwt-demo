package com.sergiovitorino.springbootjwt.infrastructure.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    // Regex: min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        boolean isValid = PASSWORD_PATTERN.matcher(password).matches();

        if (!isValid) {
            context.disableDefaultConstraintViolation();

            StringBuilder message = new StringBuilder("Password must contain: ");
            boolean needsComma = false;

            if (password.length() < 8) {
                message.append("at least 8 characters");
                needsComma = true;
            }
            if (!password.matches(".*[a-z].*")) {
                if (needsComma) message.append(", ");
                message.append("lowercase letter");
                needsComma = true;
            }
            if (!password.matches(".*[A-Z].*")) {
                if (needsComma) message.append(", ");
                message.append("uppercase letter");
                needsComma = true;
            }
            if (!password.matches(".*\\d.*")) {
                if (needsComma) message.append(", ");
                message.append("number");
                needsComma = true;
            }
            if (!password.matches(".*[@$!%*?&].*")) {
                if (needsComma) message.append(", ");
                message.append("special character (@$!%*?&)");
            }

            context.buildConstraintViolationWithTemplate(message.toString())
                   .addConstraintViolation();
        }

        return isValid;
    }
}

package com.sergiovitorino.springbootjwt.infrastructure.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SafeHtmlValidator.class})
public @interface SafeHtml {
    String message() default "Text contains html tags.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

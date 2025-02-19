package com.sporty.bookstore.inventory.dto.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated element must contain at least one non-empty field specified in {@link #fields()}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AnyFieldSetValidator.class)
public @interface AnyFieldSet {

    String message() default "One of the fields must be set";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Array of fields to validate.
     */
    String[] fields();
}
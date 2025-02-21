package com.sporty.bookstore.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Arrays;
import java.util.Objects;

public class AnyFieldSetValidator implements ConstraintValidator<AnyFieldSet, Object> {

    private String[] fields;

    @Override
    public void initialize(AnyFieldSet constraintAnnotation) {
        fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (ArrayUtils.isEmpty(fields)) {
            return false;
        }
        return Arrays.stream(fields)
                .map(field -> new BeanWrapperImpl(value).getPropertyValue(field))
                .filter(Objects::nonNull)
                .anyMatch(fieldValue -> StringUtils.isNotBlank(fieldValue.toString()));
    }
}
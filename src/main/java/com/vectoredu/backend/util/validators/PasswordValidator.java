package com.vectoredu.backend.util.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Инициализация, если требуется (в данном случае не требуется)
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;

        String regex = "^(?=.*[A-Z])(?=.*[0-9]).{8,20}$";

        return value.matches(regex);
    }
}

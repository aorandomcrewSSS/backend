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

        boolean validLength = value.length() >= 8;
        boolean hasUppercase = value.matches(".*[A-Z].*");
        boolean hasDigit = value.matches(".*[0-9].*");

        // Если какой-либо из условий не выполнен, возвращаем false
        return validLength && hasUppercase && hasDigit;
    }
}

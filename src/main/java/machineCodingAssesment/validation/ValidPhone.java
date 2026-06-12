package machineCodingAssesment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Domain-specific constraint: an (optional) phone number must be exactly 10 digits.
 * Null/blank is treated as valid here -- presence is governed separately by @NotBlank.
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface ValidPhone {
    String message() default "phone must be a 10-digit number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

package machineCodingAssesment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final Pattern TEN_DIGITS = Pattern.compile("\\d{10}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) {
            return true;   // optional field; @NotBlank handles required-ness separately
        }
        return TEN_DIGITS.matcher(value.trim()).matches();
    }
}

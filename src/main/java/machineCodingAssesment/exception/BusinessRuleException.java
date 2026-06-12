package machineCodingAssesment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation violates a domain rule -- e.g. cancelling an order
 * after pickup (rule 10), picking up a cancelled order (rule 9), or a driver
 * trying to pick up an order not assigned to them.
 * 422 distinguishes a well-formed-but-illegal action from a 400 (malformed input).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}

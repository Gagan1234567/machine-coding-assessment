package machineCodingAssesment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a referenced entity (customer, driver, item, order) does not exist.
 * The @ResponseStatus gives a correct 404 in V1 with no handler code;
 * V2 replaces this with a GlobalExceptionHandler + structured ErrorResponse body.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

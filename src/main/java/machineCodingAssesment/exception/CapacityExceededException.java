package machineCodingAssesment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the pending-order queue is full and no driver can take the order
 * right now. Maps to 503 Service Unavailable -- a load-shedding / backpressure
 * signal telling the caller to retry later, instead of letting the in-memory
 * queue grow unbounded and risk an OutOfMemoryError.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException(String message) {
        super(message);
    }
}

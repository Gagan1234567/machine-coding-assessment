package machineCodingAssesment.model;

/**
 * Order lifecycle.
 *
 * PENDING    -> created, waiting for a driver to be auto-assigned.
 * ASSIGNED   -> a driver has been auto-assigned but has not yet picked it up.
 * PICKED_UP  -> driver physically picked the parcel; can no longer be cancelled (rule 10).
 * DELIVERED  -> terminal success state.
 * CANCELLED  -> terminal state, reachable only from PENDING or ASSIGNED.
 */
public enum OrderStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}

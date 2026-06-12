package machineCodingAssesment.model;

/**
 * Lifecycle states for a driver.
 * AVAILABLE -> can be auto-assigned an order.
 * BUSY      -> currently handling exactly one order (rule 4: one order at a time).
 */
public enum DriverStatus {
    AVAILABLE,
    BUSY
}

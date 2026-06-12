package machineCodingAssesment.dto.request;

import lombok.Data;

/**
 * Onboard a new customer (rule 1).
 * Bean Validation annotations are added in V2.
 */
@Data
public class CreateCustomerRequest {
    private String name;
    private String phone;
}

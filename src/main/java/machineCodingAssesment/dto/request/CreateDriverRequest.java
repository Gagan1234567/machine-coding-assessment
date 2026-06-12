package machineCodingAssesment.dto.request;

import lombok.Data;

/**
 * Onboard a new driver (rule 1).
 * Bean Validation annotations are added in V2.
 */
@Data
public class CreateDriverRequest {
    private String name;
}

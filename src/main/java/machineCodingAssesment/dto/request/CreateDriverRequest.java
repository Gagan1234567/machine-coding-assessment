package machineCodingAssesment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import machineCodingAssesment.validation.ValidPhone;
import lombok.Data;

/**
 * Onboard a new driver (rule 1).
 */
@Data
public class CreateDriverRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    private String name;

    @ValidPhone
    private String phone;
}

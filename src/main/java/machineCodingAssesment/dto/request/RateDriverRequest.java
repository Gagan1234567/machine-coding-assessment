package machineCodingAssesment.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Bonus: customer rates the driver after delivery, on a 1-5 scale.
 */
@Data
public class RateDriverRequest {

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;
}

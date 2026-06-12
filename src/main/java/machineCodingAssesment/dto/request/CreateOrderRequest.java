package machineCodingAssesment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Place a parcel delivery order from one customer to another (rule 3).
 */
@Data
public class CreateOrderRequest {

    @NotBlank(message = "senderCustomerId is required")
    private String senderCustomerId;

    @NotBlank(message = "receiverCustomerId is required")
    private String receiverCustomerId;

    @NotBlank(message = "itemId is required")
    private String itemId;
}

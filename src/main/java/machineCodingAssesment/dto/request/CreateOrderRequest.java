package machineCodingAssesment.dto.request;

import lombok.Data;

/**
 * Place a parcel delivery order from one customer to another (rule 3).
 * Bean Validation annotations are added in V2.
 */
@Data
public class CreateOrderRequest {
    private String senderCustomerId;
    private String receiverCustomerId;
    private String itemId;
}

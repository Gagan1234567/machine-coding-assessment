package machineCodingAssesment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;                  // server-generated UUID
    private String senderCustomerId;    // customer placing the order
    private String receiverCustomerId;  // the "other" customer (rule: from one customer to another)
    private String itemId;              // references the fixed Item catalog

    private OrderStatus status;
    private String assignedDriverId;    // null until assigned

    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime updatedAt;
}

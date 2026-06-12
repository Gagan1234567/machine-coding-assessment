package machineCodingAssesment.dto.response;

import machineCodingAssesment.model.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private String id;
    private String name;
    private DriverStatus status;
    private double rating;
    private int ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

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
public class Driver {
    private String id;            // server-generated UUID
    private String name;
    private DriverStatus status;

    // Bonus (wired in a later version): average rating + count for incremental averaging.
    private double rating;
    private int ratingCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

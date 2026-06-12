package machineCodingAssesment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preconfigured, fixed catalog of deliverable items (rule 2).
 * Uses a natural-key code as the id (e.g. "DOCUMENT") rather than a UUID,
 * because the catalog is fixed and seeded at startup -- this keeps client
 * references stable and demo curls deterministic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String id;     // natural code, e.g. "DOCUMENT"
    private String name;   // human-readable label
}

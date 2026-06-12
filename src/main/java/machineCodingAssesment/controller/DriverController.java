package machineCodingAssesment.controller;

import lombok.RequiredArgsConstructor;
import machineCodingAssesment.dto.request.CreateDriverRequest;
import machineCodingAssesment.dto.response.ApiResponse;
import machineCodingAssesment.dto.response.DriverResponse;
import machineCodingAssesment.model.DriverStatus;
import machineCodingAssesment.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> onboard(@RequestBody CreateDriverRequest request) {
        DriverResponse created = driverService.onboard(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver onboarded", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(driverService.getById(id)));
    }

    // Optional status filter -> drives the "show status of drivers" requirement (rule 8).
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverResponse>>> list(
            @RequestParam(required = false) DriverStatus status) {
        List<DriverResponse> drivers = (status == null)
                ? driverService.getAll()
                : driverService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(drivers));
    }
}

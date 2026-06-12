package machineCodingAssesment.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import machineCodingAssesment.dto.request.CreateCustomerRequest;
import machineCodingAssesment.dto.response.ApiResponse;
import machineCodingAssesment.dto.response.CustomerResponse;
import machineCodingAssesment.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Onboard and view customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> onboard(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse created = customerService.onboard(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer onboarded", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAll()));
    }
}

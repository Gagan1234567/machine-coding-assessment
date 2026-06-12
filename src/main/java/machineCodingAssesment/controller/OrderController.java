package machineCodingAssesment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import machineCodingAssesment.dto.request.CreateOrderRequest;
import machineCodingAssesment.dto.request.RateDriverRequest;
import machineCodingAssesment.dto.response.ApiResponse;
import machineCodingAssesment.dto.response.OrderResponse;
import machineCodingAssesment.model.OrderStatus;
import machineCodingAssesment.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Place/cancel orders, driver pickup & deliver, rating, and status views")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place an order", description = "Creates an order and auto-assigns an idle driver; auto-cancels after 20s if none.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", created));
    }

    @Operation(summary = "Cancel an order", description = "Allowed only while PENDING/ASSIGNED; frees the driver if ASSIGNED. 422 after pickup.")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", orderService.cancelOrder(id)));
    }

    @Operation(summary = "Driver picks up the order", description = "ASSIGNED -> PICKED_UP for the matching driver.")
    @PostMapping("/{id}/pickup")
    public ResponseEntity<ApiResponse<OrderResponse>> pickup(@PathVariable String id,
                                                             @RequestParam String driverId) {
        return ResponseEntity.ok(ApiResponse.success("Order picked up", orderService.pickupOrder(id, driverId)));
    }

    @Operation(summary = "Driver delivers the order", description = "PICKED_UP -> DELIVERED; frees the driver and reassigns the queue.")
    @PostMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> deliver(@PathVariable String id,
                                                              @RequestParam String driverId) {
        return ResponseEntity.ok(ApiResponse.success("Order delivered", orderService.deliverOrder(id, driverId)));
    }

    @Operation(summary = "Rate the driver (bonus)", description = "Rate 1-5 after delivery; updates the driver's running average.")
    @PostMapping("/{id}/rate")
    public ResponseEntity<ApiResponse<OrderResponse>> rate(@PathVariable String id,
                                                           @Valid @RequestBody RateDriverRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Driver rated", orderService.rateDriver(id, request.getRating())));
    }

    @Operation(summary = "Get order by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    @Operation(summary = "List orders", description = "Optional ?status= filter (rule 8).")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(
            @RequestParam(required = false) OrderStatus status) {
        List<OrderResponse> orders = (status == null)
                ? orderService.getAll()
                : orderService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}

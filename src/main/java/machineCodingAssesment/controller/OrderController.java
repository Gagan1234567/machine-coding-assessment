package machineCodingAssesment.controller;

import lombok.RequiredArgsConstructor;
import machineCodingAssesment.dto.request.CreateOrderRequest;
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
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", created));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", orderService.cancelOrder(id)));
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<ApiResponse<OrderResponse>> pickup(@PathVariable String id,
                                                             @RequestParam String driverId) {
        return ResponseEntity.ok(ApiResponse.success("Order picked up", orderService.pickupOrder(id, driverId)));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> deliver(@PathVariable String id,
                                                              @RequestParam String driverId) {
        return ResponseEntity.ok(ApiResponse.success("Order delivered", orderService.deliverOrder(id, driverId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    // Optional status filter -> "show status of orders" (rule 8).
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(
            @RequestParam(required = false) OrderStatus status) {
        List<OrderResponse> orders = (status == null)
                ? orderService.getAll()
                : orderService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}

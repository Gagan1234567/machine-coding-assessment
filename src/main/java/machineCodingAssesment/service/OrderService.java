package machineCodingAssesment.service;

import machineCodingAssesment.dto.request.CreateOrderRequest;
import machineCodingAssesment.dto.response.OrderResponse;
import machineCodingAssesment.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(CreateOrderRequest request);
    OrderResponse cancelOrder(String orderId);
    OrderResponse pickupOrder(String orderId, String driverId);
    OrderResponse deliverOrder(String orderId, String driverId);
    OrderResponse getById(String orderId);
    List<OrderResponse> getAll();
    List<OrderResponse> getByStatus(OrderStatus status);
}

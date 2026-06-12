package machineCodingAssesment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import machineCodingAssesment.dto.request.CreateOrderRequest;
import machineCodingAssesment.dto.response.OrderResponse;
import machineCodingAssesment.exception.BusinessRuleException;
import machineCodingAssesment.exception.CapacityExceededException;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.OrderStatus;
import machineCodingAssesment.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private OrderService orderService;

    private CreateOrderRequest validRequest() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setSenderCustomerId("c1");
        req.setReceiverCustomerId("c2");
        req.setItemId("DOCUMENT");
        return req;
    }

    @Test
    void shouldReturn201_whenPlaceOrderValid() throws Exception {
        when(orderService.placeOrder(any()))
                .thenReturn(OrderResponse.builder().id("o1").status(OrderStatus.ASSIGNED).build());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));
    }

    @Test
    void shouldReturn400_whenSenderCustomerIdBlank() throws Exception {
        CreateOrderRequest req = validRequest();
        req.setSenderCustomerId("  ");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.senderCustomerId").exists());
    }

    @Test
    void shouldReturn404_whenOrderNotFound() throws Exception {
        when(orderService.getById("nope")).thenThrow(new ResourceNotFoundException("Order not found with id: nope"));

        mockMvc.perform(get("/api/orders/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn422_whenCancelAfterPickup() throws Exception {
        when(orderService.cancelOrder("o1")).thenThrow(new BusinessRuleException("already picked up"));

        mockMvc.perform(post("/api/orders/o1/cancel"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void shouldReturn200_whenPickupValid() throws Exception {
        when(orderService.pickupOrder("o1", "d1"))
                .thenReturn(OrderResponse.builder().id("o1").status(OrderStatus.PICKED_UP).build());

        mockMvc.perform(post("/api/orders/o1/pickup").param("driverId", "d1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PICKED_UP"));
    }

    @Test
    void shouldReturn400_whenPickupMissingDriverId() throws Exception {
        mockMvc.perform(post("/api/orders/o1/pickup"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenRatingOutOfRange() throws Exception {
        mockMvc.perform(post("/api/orders/o1/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.rating").exists());
    }

    @Test
    void shouldReturn200_whenRatingValid() throws Exception {
        when(orderService.rateDriver(eq("o1"), anyInt()))
                .thenReturn(OrderResponse.builder().id("o1").status(OrderStatus.DELIVERED).rating(5).build());

        mockMvc.perform(post("/api/orders/o1/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    void shouldReturn503_whenQueueFull() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new CapacityExceededException("Pending order queue is full"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void shouldReturn400_whenStatusFilterIsInvalidEnum() throws Exception {
        mockMvc.perform(get("/api/orders").param("status", "FOO"))
                .andExpect(status().isBadRequest());
    }
}

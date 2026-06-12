package machineCodingAssesment.service;

import machineCodingAssesment.dto.request.CreateOrderRequest;
import machineCodingAssesment.dto.response.OrderResponse;
import machineCodingAssesment.exception.BusinessRuleException;
import machineCodingAssesment.exception.CapacityExceededException;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.Driver;
import machineCodingAssesment.model.DriverStatus;
import machineCodingAssesment.model.Order;
import machineCodingAssesment.model.OrderStatus;
import machineCodingAssesment.repository.CustomerRepository;
import machineCodingAssesment.repository.DriverRepository;
import machineCodingAssesment.repository.ItemRepository;
import machineCodingAssesment.repository.OrderRepository;
import machineCodingAssesment.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private TaskScheduler taskScheduler;

    private OrderServiceImpl service;

    private static final int MAX_PENDING = 1000;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderRepository, driverRepository,
                customerRepository, itemRepository, taskScheduler, MAX_PENDING);
        // valid references by default
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(itemRepository.existsById(anyString())).thenReturn(true);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(driverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private CreateOrderRequest orderRequest() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setSenderCustomerId("c1");
        req.setReceiverCustomerId("c2");
        req.setItemId("DOCUMENT");
        return req;
    }

    private Driver availableDriver(String id, double rating) {
        return Driver.builder().id(id).name("D" + id).status(DriverStatus.AVAILABLE)
                .rating(rating).ratingCount(0).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void shouldAutoAssignOrder_whenIdleDriverExists() {
        Driver driver = availableDriver("d1", 0);
        // findById returns whatever was last saved (order is mutated in place)
        final Order[] saved = new Order[1];
        when(orderRepository.save(any())).thenAnswer(inv -> { saved[0] = inv.getArgument(0); return saved[0]; });
        when(orderRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved[0]));
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of(driver));

        OrderResponse res = service.placeOrder(orderRequest());

        assertThat(res.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(res.getAssignedDriverId()).isEqualTo("d1");
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.BUSY);
        verify(taskScheduler).schedule(any(Runnable.class), any(java.time.Instant.class));
    }

    @Test
    void shouldKeepOrderPending_whenNoDriverAvailable() {
        final Order[] saved = new Order[1];
        when(orderRepository.save(any())).thenAnswer(inv -> { saved[0] = inv.getArgument(0); return saved[0]; });
        when(orderRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved[0]));
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of());

        OrderResponse res = service.placeOrder(orderRequest());

        assertThat(res.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(res.getAssignedDriverId()).isNull();
    }

    @Test
    void shouldPreferHigherRatedDriver_duringAssignment() {
        Driver low = availableDriver("low", 3.0);
        Driver high = availableDriver("high", 4.8);
        final Order[] saved = new Order[1];
        when(orderRepository.save(any())).thenAnswer(inv -> { saved[0] = inv.getArgument(0); return saved[0]; });
        when(orderRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved[0]));
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of(low, high));

        OrderResponse res = service.placeOrder(orderRequest());

        assertThat(res.getAssignedDriverId()).isEqualTo("high");
    }

    @Test
    void shouldThrowNotFound_whenItemDoesNotExist() {
        when(itemRepository.existsById("DOCUMENT")).thenReturn(false);
        assertThatThrownBy(() -> service.placeOrder(orderRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFreeDriverAndReassign_whenAssignedOrderCancelled() {
        Driver driver = availableDriver("d1", 0);
        driver.setStatus(DriverStatus.BUSY);
        Order order = Order.builder().id("o1").status(OrderStatus.ASSIGNED).assignedDriverId("d1")
                .createdAt(LocalDateTime.now()).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(driverRepository.findById("d1")).thenReturn(Optional.of(driver));
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of());

        OrderResponse res = service.cancelOrder("o1");

        assertThat(res.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void shouldThrowBusinessRule_whenCancellingAfterPickup() {
        Order order = Order.builder().id("o1").status(OrderStatus.PICKED_UP).assignedDriverId("d1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> service.cancelOrder("o1"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldPickUpOrder_whenAssignedToSameDriver() {
        Order order = Order.builder().id("o1").status(OrderStatus.ASSIGNED).assignedDriverId("d1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(driverRepository.existsById("d1")).thenReturn(true);

        OrderResponse res = service.pickupOrder("o1", "d1");

        assertThat(res.getStatus()).isEqualTo(OrderStatus.PICKED_UP);
    }

    @Test
    void shouldThrowBusinessRule_whenPickupByWrongDriver() {
        Order order = Order.builder().id("o1").status(OrderStatus.ASSIGNED).assignedDriverId("d1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(driverRepository.existsById("d2")).thenReturn(true);
        assertThatThrownBy(() -> service.pickupOrder("o1", "d2"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldThrowBusinessRule_whenPickupCancelledOrder() {
        Order order = Order.builder().id("o1").status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(driverRepository.existsById("d1")).thenReturn(true);
        assertThatThrownBy(() -> service.pickupOrder("o1", "d1"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldDeliverOrderAndFreeDriver_whenPickedUp() {
        Driver driver = availableDriver("d1", 0);
        driver.setStatus(DriverStatus.BUSY);
        Order order = Order.builder().id("o1").status(OrderStatus.PICKED_UP).assignedDriverId("d1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(driverRepository.existsById("d1")).thenReturn(true);
        when(driverRepository.findById("d1")).thenReturn(Optional.of(driver));
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of());

        OrderResponse res = service.deliverOrder("o1", "d1");

        assertThat(res.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void shouldThrowNotFound_whenOrderDoesNotExist() {
        when(orderRepository.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateDriverAverage_whenRatingAfterDelivery() {
        Driver driver = availableDriver("d1", 0);
        Order order = Order.builder().id("o1").status(OrderStatus.DELIVERED).assignedDriverId("d1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(driverRepository.findById("d1")).thenReturn(Optional.of(driver));

        OrderResponse res = service.rateDriver("o1", 5);

        assertThat(res.getRating()).isEqualTo(5);
        assertThat(driver.getRating()).isEqualTo(5.0);
        assertThat(driver.getRatingCount()).isEqualTo(1);
    }

    @Test
    void shouldThrowBusinessRule_whenRatingBeforeDelivery() {
        Order order = Order.builder().id("o1").status(OrderStatus.ASSIGNED).assignedDriverId("d1").build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> service.rateDriver("o1", 4))
                .isInstanceOf(BusinessRuleException.class);
        verify(driverRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRule_whenRatingTwice() {
        Driver driver = availableDriver("d1", 5);
        driver.setRatingCount(1);
        Order order = Order.builder().id("o1").status(OrderStatus.DELIVERED).assignedDriverId("d1").rating(5).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> service.rateDriver("o1", 4))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldRejectOrder_whenQueueFullAndNoDriver() {
        // cap = 1: first order fills the single slot, second is shed with 503.
        OrderServiceImpl bounded = new OrderServiceImpl(orderRepository, driverRepository,
                customerRepository, itemRepository, taskScheduler, 1);
        final Order[] saved = new Order[1];
        when(orderRepository.save(any())).thenAnswer(inv -> { saved[0] = inv.getArgument(0); return saved[0]; });
        when(orderRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved[0]));
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of());

        bounded.placeOrder(orderRequest());   // queued (PENDING), no driver -> fills the slot

        assertThatThrownBy(() -> bounded.placeOrder(orderRequest()))
                .isInstanceOf(CapacityExceededException.class);
    }

    @Test
    void shouldAcceptOrder_whenQueueFullButDriverAvailable() {
        // even at capacity, an idle driver means the order is served, not shed.
        OrderServiceImpl bounded = new OrderServiceImpl(orderRepository, driverRepository,
                customerRepository, itemRepository, taskScheduler, 1);
        final Order[] saved = new Order[1];
        when(orderRepository.save(any())).thenAnswer(inv -> { saved[0] = inv.getArgument(0); return saved[0]; });
        when(orderRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved[0]));
        // no driver for the first (it queues), then a driver is available
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE))
                .thenReturn(List.of())
                .thenReturn(List.of(availableDriver("d1", 0)));

        bounded.placeOrder(orderRequest());   // fills the single slot

        // queue is full, but a driver is available -> accepted, NOT shed with 503
        assertThatCode(() -> bounded.placeOrder(orderRequest())).doesNotThrowAnyException();
    }

    @Test
    void shouldAutoCancel_whenTimeoutFiresAndStillPending() {
        // Capture the scheduled timeout task, then invoke it to simulate the 20s elapse.
        Driver none = null;
        final Order[] saved = new Order[1];
        when(orderRepository.save(any())).thenAnswer(inv -> { saved[0] = inv.getArgument(0); return saved[0]; });
        when(orderRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved[0]));
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of());

        final Runnable[] timeoutTask = new Runnable[1];
        when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
                .thenAnswer(inv -> { timeoutTask[0] = inv.getArgument(0); return null; });

        service.placeOrder(orderRequest());
        assertThat(saved[0].getStatus()).isEqualTo(OrderStatus.PENDING);

        timeoutTask[0].run();   // simulate the 20s timeout firing

        assertThat(saved[0].getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}

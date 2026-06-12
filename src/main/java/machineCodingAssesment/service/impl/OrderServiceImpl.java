package machineCodingAssesment.service.impl;

import lombok.extern.slf4j.Slf4j;
import machineCodingAssesment.dto.request.CreateOrderRequest;
import machineCodingAssesment.dto.response.OrderResponse;
import machineCodingAssesment.exception.BusinessRuleException;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.Driver;
import machineCodingAssesment.model.DriverStatus;
import machineCodingAssesment.model.Order;
import machineCodingAssesment.model.OrderStatus;
import machineCodingAssesment.repository.CustomerRepository;
import machineCodingAssesment.repository.DriverRepository;
import machineCodingAssesment.repository.ItemRepository;
import machineCodingAssesment.repository.OrderRepository;
import machineCodingAssesment.service.OrderService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Core orchestrator: order lifecycle + auto-assignment to idle drivers.
 *
 * Concurrency posture (V1):
 *  - A real second thread exists from day one (the timeout scheduler), so all
 *    state transitions + the assignment critical section are serialized through
 *    a single ReentrantLock for correctness. This directly satisfies the Locking
 *    Note (one driver -> one order; a cancelled/already-picked order cannot be
 *    picked by a concurrent request).
 *  - V3 will replace this coarse lock with granular / read-write locking and
 *    swap the HashMap stores for ConcurrentHashMap.
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    // Business rule (rule 7): unassigned orders are auto-cancelled after this window.
    private static final Duration ASSIGNMENT_TIMEOUT = Duration.ofSeconds(20);

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final TaskScheduler taskScheduler;

    // FIFO queue of order ids waiting for a driver. Ongoing orders may exceed
    // driver count (rule 5), so this can grow beyond the number of drivers.
    private final Deque<String> pendingQueue = new LinkedList<>();

    // Coarse lock guarding all state transitions + the pending queue.
    private final ReentrantLock lock = new ReentrantLock();

    public OrderServiceImpl(OrderRepository orderRepository,
                            DriverRepository driverRepository,
                            CustomerRepository customerRepository,
                            ItemRepository itemRepository,
                            TaskScheduler taskScheduler) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public OrderResponse placeOrder(CreateOrderRequest request) {
        validateReferences(request);

        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .senderCustomerId(request.getSenderCustomerId())
                .receiverCustomerId(request.getReceiverCustomerId())
                .itemId(request.getItemId())
                .status(OrderStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        lock.lock();
        try {
            orderRepository.save(order);
            pendingQueue.addLast(order.getId());
            log.info("Order placed: id={}, item={}, status=PENDING", order.getId(), order.getItemId());
            // Try to assign immediately if a driver is idle.
            assignPendingOrders();
        } finally {
            lock.unlock();
        }

        // Schedule the 20s auto-cancel; the task is a no-op if the order is no
        // longer PENDING by then.
        scheduleAssignmentTimeout(order.getId());

        // Re-read so the response reflects any immediate assignment.
        return toResponse(findOrderOrThrow(order.getId()));
    }

    @Override
    public OrderResponse cancelOrder(String orderId) {
        lock.lock();
        try {
            Order order = findOrderOrThrow(orderId);
            switch (order.getStatus()) {
                case PENDING -> {
                    pendingQueue.remove(orderId);
                    markCancelled(order);
                    log.info("Order cancelled by user while PENDING: id={}", orderId);
                }
                case ASSIGNED -> {
                    // rule 9: cancelling an assigned (not yet picked) order frees the driver.
                    String driverId = order.getAssignedDriverId();
                    markCancelled(order);
                    freeDriver(driverId);
                    log.info("Order cancelled by user while ASSIGNED: id={}, freedDriver={}", orderId, driverId);
                    // Freed driver can now take the next waiting order.
                    assignPendingOrders();
                }
                // rule 10: once picked up, the order can no longer be cancelled.
                case PICKED_UP -> throw new BusinessRuleException(
                        "Order " + orderId + " is already picked up and cannot be cancelled");
                case DELIVERED -> throw new BusinessRuleException(
                        "Order " + orderId + " is already delivered and cannot be cancelled");
                case CANCELLED -> throw new BusinessRuleException(
                        "Order " + orderId + " is already cancelled");
            }
            return toResponse(findOrderOrThrow(orderId));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderResponse pickupOrder(String orderId, String driverId) {
        lock.lock();
        try {
            requireDriverExists(driverId);
            Order order = findOrderOrThrow(orderId);

            if (order.getStatus() == OrderStatus.CANCELLED) {
                // rule 9: a cancelled order is never available for pickup.
                throw new BusinessRuleException("Order " + orderId + " is cancelled and cannot be picked up");
            }
            if (order.getStatus() != OrderStatus.ASSIGNED) {
                throw new BusinessRuleException(
                        "Order " + orderId + " is not in ASSIGNED state (current: " + order.getStatus() + ")");
            }
            if (!driverId.equals(order.getAssignedDriverId())) {
                throw new BusinessRuleException(
                        "Order " + orderId + " is not assigned to driver " + driverId);
            }

            LocalDateTime now = LocalDateTime.now();
            order.setStatus(OrderStatus.PICKED_UP);
            order.setPickedUpAt(now);
            order.setUpdatedAt(now);
            orderRepository.save(order);
            log.info("Order picked up: id={}, driver={}", orderId, driverId);
            return toResponse(order);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderResponse deliverOrder(String orderId, String driverId) {
        lock.lock();
        try {
            requireDriverExists(driverId);
            Order order = findOrderOrThrow(orderId);

            if (order.getStatus() != OrderStatus.PICKED_UP) {
                throw new BusinessRuleException(
                        "Order " + orderId + " is not in PICKED_UP state (current: " + order.getStatus() + ")");
            }
            if (!driverId.equals(order.getAssignedDriverId())) {
                throw new BusinessRuleException(
                        "Order " + orderId + " is not assigned to driver " + driverId);
            }

            LocalDateTime now = LocalDateTime.now();
            order.setStatus(OrderStatus.DELIVERED);
            order.setDeliveredAt(now);
            order.setUpdatedAt(now);
            orderRepository.save(order);
            log.info("Order delivered: id={}, driver={}", orderId, driverId);

            // Driver is free again -> can serve the next waiting order.
            freeDriver(driverId);
            assignPendingOrders();
            return toResponse(order);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderResponse getById(String orderId) {
        return toResponse(findOrderOrThrow(orderId));
    }

    @Override
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------------
    // Internal assignment + lifecycle helpers (all called under `lock`)
    // ---------------------------------------------------------------------

    /**
     * Drains the pending queue against currently idle drivers (rule 5).
     * Stops when either the queue is empty or no driver is available.
     */
    private void assignPendingOrders() {
        while (!pendingQueue.isEmpty()) {
            String orderId = pendingQueue.peekFirst();
            Order order = orderRepository.findById(orderId).orElse(null);

            // Drop stale entries (e.g. an order cancelled while queued).
            if (order == null || order.getStatus() != OrderStatus.PENDING) {
                pendingQueue.pollFirst();
                continue;
            }

            Driver driver = pickAvailableDriver();
            if (driver == null) {
                // No idle driver -> leave the order queued; it will be picked up
                // when a driver frees, or auto-cancelled at the 20s timeout.
                break;
            }

            pendingQueue.pollFirst();
            assign(order, driver);
        }
    }

    /**
     * V1: first idle driver. V2 (bonus) will prefer the highest-rated driver.
     */
    private Driver pickAvailableDriver() {
        List<Driver> available = driverRepository.findByStatus(DriverStatus.AVAILABLE);
        return available.isEmpty() ? null : available.get(0);
    }

    private void assign(Order order, Driver driver) {
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.ASSIGNED);
        order.setAssignedDriverId(driver.getId());
        order.setAssignedAt(now);
        order.setUpdatedAt(now);
        orderRepository.save(order);

        driver.setStatus(DriverStatus.BUSY);
        driver.setUpdatedAt(now);
        driverRepository.save(driver);

        log.info("Order auto-assigned: order={}, driver={}", order.getId(), driver.getId());
    }

    private void freeDriver(String driverId) {
        if (driverId == null) {
            return;
        }
        driverRepository.findById(driverId).ifPresent(driver -> {
            driver.setStatus(DriverStatus.AVAILABLE);
            driver.setUpdatedAt(LocalDateTime.now());
            driverRepository.save(driver);
            log.info("Driver is now available: id={}", driverId);
        });
    }

    private void markCancelled(Order order) {
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.CANCELLED);
        order.setAssignedDriverId(null);
        order.setCancelledAt(now);
        order.setUpdatedAt(now);
        orderRepository.save(order);
    }

    /**
     * Schedules the 20s auto-cancel (rule 7). Runs on the scheduler thread and
     * takes the same lock, so it is consistent with user-driven transitions.
     */
    private void scheduleAssignmentTimeout(String orderId) {
        taskScheduler.schedule(() -> {
            lock.lock();
            try {
                Order order = orderRepository.findById(orderId).orElse(null);
                // Only cancel if STILL waiting for a driver. If assigned/picked/
                // delivered/cancelled in the meantime, do nothing.
                if (order != null && order.getStatus() == OrderStatus.PENDING) {
                    pendingQueue.remove(orderId);
                    markCancelled(order);
                    log.info("Order auto-cancelled after {}s with no driver: id={}",
                            ASSIGNMENT_TIMEOUT.toSeconds(), orderId);
                }
            } finally {
                lock.unlock();
            }
        }, Instant.now().plus(ASSIGNMENT_TIMEOUT));
    }

    private void validateReferences(CreateOrderRequest request) {
        if (!customerRepository.existsById(request.getSenderCustomerId())) {
            throw new ResourceNotFoundException("Sender customer not found with id: " + request.getSenderCustomerId());
        }
        if (!customerRepository.existsById(request.getReceiverCustomerId())) {
            throw new ResourceNotFoundException("Receiver customer not found with id: " + request.getReceiverCustomerId());
        }
        if (!itemRepository.existsById(request.getItemId())) {
            throw new ResourceNotFoundException("Item not found with id: " + request.getItemId());
        }
    }

    private void requireDriverExists(String driverId) {
        if (driverId == null || !driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver not found with id: " + driverId);
        }
    }

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .senderCustomerId(o.getSenderCustomerId())
                .receiverCustomerId(o.getReceiverCustomerId())
                .itemId(o.getItemId())
                .status(o.getStatus())
                .assignedDriverId(o.getAssignedDriverId())
                .createdAt(o.getCreatedAt())
                .assignedAt(o.getAssignedAt())
                .pickedUpAt(o.getPickedUpAt())
                .deliveredAt(o.getDeliveredAt())
                .cancelledAt(o.getCancelledAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}

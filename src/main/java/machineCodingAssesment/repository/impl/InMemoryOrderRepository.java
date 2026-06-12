package machineCodingAssesment.repository.impl;

import machineCodingAssesment.model.Order;
import machineCodingAssesment.model.OrderStatus;
import machineCodingAssesment.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * V1 in-memory store backed by a plain HashMap.
 * V3 upgrades to ConcurrentHashMap.
 */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new HashMap<>();

    @Override
    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return store.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }
}

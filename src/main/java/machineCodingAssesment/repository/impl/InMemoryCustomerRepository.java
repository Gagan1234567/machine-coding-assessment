package machineCodingAssesment.repository.impl;

import machineCodingAssesment.model.Customer;
import machineCodingAssesment.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * V1 in-memory store backed by a plain HashMap.
 * V3 upgrades to ConcurrentHashMap for lock-free concurrent get/put.
 */
@Repository
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> store = new HashMap<>();

    @Override
    public Customer save(Customer customer) {
        store.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }
}

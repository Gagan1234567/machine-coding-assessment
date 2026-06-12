package machineCodingAssesment.repository.impl;

import machineCodingAssesment.model.Driver;
import machineCodingAssesment.model.DriverStatus;
import machineCodingAssesment.repository.DriverRepository;
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
public class InMemoryDriverRepository implements DriverRepository {

    private final Map<String, Driver> store = new HashMap<>();

    @Override
    public Driver save(Driver driver) {
        store.put(driver.getId(), driver);
        return driver;
    }

    @Override
    public Optional<Driver> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Driver> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Driver> findByStatus(DriverStatus status) {
        return store.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }
}

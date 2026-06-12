package machineCodingAssesment.repository.impl;

import machineCodingAssesment.model.Item;
import machineCodingAssesment.repository.ItemRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * V1 in-memory store for the fixed item catalog (seeded at startup).
 */
@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<String, Item> store = new HashMap<>();

    @Override
    public Item save(Item item) {
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }
}

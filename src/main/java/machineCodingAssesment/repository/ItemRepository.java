package machineCodingAssesment.repository;

import machineCodingAssesment.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(String id);
    List<Item> findAll();
    boolean existsById(String id);
}

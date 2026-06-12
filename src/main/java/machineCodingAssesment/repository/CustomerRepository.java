package machineCodingAssesment.repository;

import machineCodingAssesment.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(String id);
    List<Customer> findAll();
    boolean existsById(String id);
}

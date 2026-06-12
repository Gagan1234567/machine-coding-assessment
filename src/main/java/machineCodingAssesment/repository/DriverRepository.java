package machineCodingAssesment.repository;

import machineCodingAssesment.model.Driver;
import machineCodingAssesment.model.DriverStatus;

import java.util.List;
import java.util.Optional;

public interface DriverRepository {
    Driver save(Driver driver);
    Optional<Driver> findById(String id);
    List<Driver> findAll();
    List<Driver> findByStatus(DriverStatus status);
    boolean existsById(String id);
}

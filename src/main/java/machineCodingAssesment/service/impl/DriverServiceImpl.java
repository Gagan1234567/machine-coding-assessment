package machineCodingAssesment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import machineCodingAssesment.dto.request.CreateDriverRequest;
import machineCodingAssesment.dto.response.DriverResponse;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.Driver;
import machineCodingAssesment.model.DriverStatus;
import machineCodingAssesment.repository.DriverRepository;
import machineCodingAssesment.service.DriverService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    @Override
    public DriverResponse onboard(CreateDriverRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Driver driver = Driver.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName() == null ? null : request.getName().trim())
                .status(DriverStatus.AVAILABLE)   // new drivers start idle and assignable
                .rating(0.0)
                .ratingCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        driverRepository.save(driver);
        log.info("Driver onboarded: id={}, name={}, status={}",
                driver.getId(), driver.getName(), driver.getStatus());
        return toResponse(driver);
    }

    @Override
    public DriverResponse getById(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        return toResponse(driver);
    }

    @Override
    public List<DriverResponse> getAll() {
        return driverRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getByStatus(DriverStatus status) {
        return driverRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DriverResponse toResponse(Driver d) {
        return DriverResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .status(d.getStatus())
                .rating(d.getRating())
                .ratingCount(d.getRatingCount())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}

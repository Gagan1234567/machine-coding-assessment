package machineCodingAssesment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import machineCodingAssesment.dto.request.CreateCustomerRequest;
import machineCodingAssesment.dto.response.CustomerResponse;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.Customer;
import machineCodingAssesment.repository.CustomerRepository;
import machineCodingAssesment.service.CustomerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerResponse onboard(CreateCustomerRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder()
                .id(UUID.randomUUID().toString())
                .name(trim(request.getName()))
                .phone(trim(request.getPhone()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        customerRepository.save(customer);
        log.info("Customer onboarded: id={}, name={}", customer.getId(), customer.getName());
        return toResponse(customer);
    }

    @Override
    public CustomerResponse getById(String id) {
        return toResponse(findCustomerOrThrow(id));
    }

    @Override
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Customer findCustomerOrThrow(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .phone(c.getPhone())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}

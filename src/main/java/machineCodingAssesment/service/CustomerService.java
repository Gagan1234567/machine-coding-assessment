package machineCodingAssesment.service;

import machineCodingAssesment.dto.request.CreateCustomerRequest;
import machineCodingAssesment.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse onboard(CreateCustomerRequest request);
    CustomerResponse getById(String id);
    List<CustomerResponse> getAll();
}

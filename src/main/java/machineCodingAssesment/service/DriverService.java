package machineCodingAssesment.service;

import machineCodingAssesment.dto.request.CreateDriverRequest;
import machineCodingAssesment.dto.response.DriverResponse;
import machineCodingAssesment.model.DriverStatus;

import java.util.List;

public interface DriverService {
    DriverResponse onboard(CreateDriverRequest request);
    DriverResponse getById(String id);
    List<DriverResponse> getAll();
    List<DriverResponse> getByStatus(DriverStatus status);
}

package machineCodingAssesment.service;

import machineCodingAssesment.dto.request.CreateDriverRequest;
import machineCodingAssesment.dto.response.DriverResponse;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.DriverStatus;
import machineCodingAssesment.repository.DriverRepository;
import machineCodingAssesment.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock private DriverRepository driverRepository;
    private DriverServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DriverServiceImpl(driverRepository);
    }

    @Test
    void shouldOnboardDriver_asAvailable_whenRequestIsValid() {
        when(driverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CreateDriverRequest req = new CreateDriverRequest();
        req.setName("Dave");
        req.setPhone("9990001111");

        DriverResponse res = service.onboard(req);

        assertThat(res.getId()).isNotBlank();
        assertThat(res.getPhone()).isEqualTo("9990001111");
        assertThat(res.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        assertThat(res.getRatingCount()).isZero();
    }

    @Test
    void shouldThrowNotFound_whenDriverIdDoesNotExist() {
        when(driverRepository.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

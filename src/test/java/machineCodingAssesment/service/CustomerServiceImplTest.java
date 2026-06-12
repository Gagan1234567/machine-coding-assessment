package machineCodingAssesment.service;

import machineCodingAssesment.dto.request.CreateCustomerRequest;
import machineCodingAssesment.dto.response.CustomerResponse;
import machineCodingAssesment.exception.ResourceNotFoundException;
import machineCodingAssesment.model.Customer;
import machineCodingAssesment.repository.CustomerRepository;
import machineCodingAssesment.service.impl.CustomerServiceImpl;
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
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void shouldOnboardCustomer_andTrimName_whenRequestIsValid() {
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("  Alice  ");
        req.setPhone("9990001111");

        CustomerResponse res = service.onboard(req);

        assertThat(res.getId()).isNotBlank();
        assertThat(res.getName()).isEqualTo("Alice");
        assertThat(res.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldThrowNotFound_whenCustomerIdDoesNotExist() {
        when(customerRepository.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnCustomer_whenIdExists() {
        when(customerRepository.findById("c1"))
                .thenReturn(Optional.of(Customer.builder().id("c1").name("Bob").build()));
        assertThat(service.getById("c1").getName()).isEqualTo("Bob");
    }
}

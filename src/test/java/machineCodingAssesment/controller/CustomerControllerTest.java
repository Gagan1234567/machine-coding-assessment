package machineCodingAssesment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import machineCodingAssesment.dto.request.CreateCustomerRequest;
import machineCodingAssesment.dto.response.CustomerResponse;
import machineCodingAssesment.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CustomerService customerService;

    @Test
    void shouldReturn201_whenCustomerValid() throws Exception {
        when(customerService.onboard(any()))
                .thenReturn(CustomerResponse.builder().id("c1").name("Alice").build());
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("Alice");
        req.setPhone("9990001111");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Alice"));
    }

    @Test
    void shouldReturn400_whenNameBlank() throws Exception {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void shouldReturn400_whenPhoneInvalid_customConstraint() throws Exception {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("Alice");
        req.setPhone("12ab");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }
}

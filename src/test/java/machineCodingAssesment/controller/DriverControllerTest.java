package machineCodingAssesment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import machineCodingAssesment.dto.request.CreateDriverRequest;
import machineCodingAssesment.dto.response.DriverResponse;
import machineCodingAssesment.model.DriverStatus;
import machineCodingAssesment.service.DriverService;
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

@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DriverService driverService;

    @Test
    void shouldReturn201_whenDriverValid() throws Exception {
        when(driverService.onboard(any()))
                .thenReturn(DriverResponse.builder().id("d1").name("Dave")
                        .phone("9990001111").status(DriverStatus.AVAILABLE).build());
        CreateDriverRequest req = new CreateDriverRequest();
        req.setName("Dave");
        req.setPhone("9990001111");

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.phone").value("9990001111"));
    }

    @Test
    void shouldReturn400_whenPhoneInvalid_customConstraint() throws Exception {
        CreateDriverRequest req = new CreateDriverRequest();
        req.setName("Dave");
        req.setPhone("12ab");

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }
}

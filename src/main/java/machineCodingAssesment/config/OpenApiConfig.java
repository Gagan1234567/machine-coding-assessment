package machineCodingAssesment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger metadata. The UI auto-documents every controller endpoint.
 * Swagger UI: /swagger-ui.html   ·   OpenAPI JSON: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI p2pDeliveryOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Peer-to-Peer Delivery System API")
                .description("Onboard customers/drivers, place parcel orders with auto-assignment "
                        + "to idle drivers, 20s auto-cancel, pickup/deliver lifecycle, and driver rating.")
                .version("v2")
                .license(new License().name("Internal assessment")));
    }
}

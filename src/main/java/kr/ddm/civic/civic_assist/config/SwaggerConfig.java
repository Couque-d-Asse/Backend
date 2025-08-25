package kr.ddm.civic.civic_assist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI civicAssistOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Civic Assist API")
                        .description("동대문구 민원 자동화 서비스 API")
                        .version("v1.0.0"));
    }
}

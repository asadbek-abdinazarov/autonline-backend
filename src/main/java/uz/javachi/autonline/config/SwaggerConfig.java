package uz.javachi.autonline.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Autonline API Documentation")
                        .description("""
                                ## Autonline is an online learning platform that provides a variety of courses to help individuals enhance their skills and knowledge.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Asadbek Abdinazarov")
                                .email("a.abdinazarov@student.pdp.university")
                                .url("https://github.com/asadbek-abdinazarov"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Test Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token here. You can get this token by logging in through the /api/v1/auth/login endpoint."))
                );

    }
/*
    @Bean
    public GroupedOpenApi authGroupAPI() {
        return GroupedOpenApi.builder()
                .group("Auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi lessonAPI() {
        return GroupedOpenApi.builder()
                .group("Lesson")
                .pathsToMatch("/api/v1/lesson/**")
                .build();
    }
    @Bean
    public GroupedOpenApi usersGroupAPI() {
        return GroupedOpenApi.builder()
                .group("Admin")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentHistoryGroupAPI() {
        return GroupedOpenApi.builder()
                .group("Payment History")
                .pathsToMatch("/api/v1/payment-history/**")
                .build();
    }

    @Bean
    public GroupedOpenApi newsGroupAPI() {
        return GroupedOpenApi.builder()
                .group("News")
                .pathsToMatch("/api/v1/news/**")
                .build();
    }

    @Bean
    public GroupedOpenApi lessonHistoryGroupAPI() {
        return GroupedOpenApi.builder()
                .group("Lesson History")
                .pathsToMatch("/api/v1/lesson-history/**")
                .build();
    }

    @Bean
    public GroupedOpenApi randomQuizGroupAPI() {
        return GroupedOpenApi.builder()
                .group("Random Quiz")
                .pathsToMatch("/api/v1/random-quiz/**")
                .build();
    }*/
}

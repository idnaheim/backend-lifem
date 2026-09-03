package com.idnaheim.lifem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI lifemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifeM API")
                        .description("Personal finance and life management REST API. Manage accounts, expenses, incomes, transactions, calendar events, and the password vault.")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("LifeM")
                                .email("support@idnaheim.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }
}

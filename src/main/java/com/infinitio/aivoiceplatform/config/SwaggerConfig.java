package com.infinitio.aivoiceplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
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

/**
 * Swagger Configuration.
 *
 * Configures OpenAPI documentation and JWT authentication
 * for Swagger UI.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {

        Server localServer = new Server();

        localServer.setUrl("http://localhost:8080");

        localServer.setDescription(
                "Local Development Server"
        );

        return new OpenAPI()

                .servers(
                        List.of(localServer)
                )

                .info(

                        new Info()

                                .title(
                                        "AI Voice Platform API"
                                )

                                .version(
                                        "v1.0.0"
                                )

                                .description("""
                                        Enterprise AI Voice Platform APIs

                                        Features
                                        - Authentication
                                        - Organization Management
                                        - User Management
                                        - Role & Permission
                                        - AI Agent
                                        - AI Dialer
                                        - Campaign
                                        - Knowledge Base
                                        - Conversation
                                        - Analytics
                                        """)

                                .contact(

                                        new Contact()

                                                .name(
                                                        "Infinitio Digital"
                                                )

                                                .email(
                                                        "support@infinitiodigital.com"
                                                )

                                                .url(
                                                        "https://infinitiodigital.com"
                                                )
                                )

                                .license(

                                        new License()

                                                .name(
                                                        "Proprietary"
                                                )

                                                .url(
                                                        "https://infinitiodigital.com"
                                                )
                                )
                )

                /*
                 * JWT Bearer Authentication
                 */
                .components(

                        new Components()

                                .addSecuritySchemes(

                                        SECURITY_SCHEME_NAME,

                                        new SecurityScheme()

                                                .name(
                                                        "Authorization"
                                                )

                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )

                                                .scheme(
                                                        "bearer"
                                                )

                                                .bearerFormat(
                                                        "JWT"
                                                )

                                                .description(
                                                        "Enter your JWT access token."
                                                )
                                )
                )

                /*
                 * Apply JWT authentication to APIs
                 * unless an endpoint explicitly overrides it.
                 */
                .addSecurityItem(

                        new SecurityRequirement()

                                .addList(
                                        SECURITY_SCHEME_NAME
                                )
                )

                .externalDocs(

                        new ExternalDocumentation()

                                .description(
                                        "AI Voice Platform Documentation"
                                )

                                .url(
                                        "https://docs.infinitiodigital.com"
                                )
                );
    }
}
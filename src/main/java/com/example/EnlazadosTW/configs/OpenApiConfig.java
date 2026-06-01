package com.example.EnlazadosTW.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para la documentación automática de APIs.
 * Accede a través de: http://localhost:8080/api/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("EnlazadosTW API")
				.description("Plataforma de seguimiento terapéutico-educativo para pacientes con capacidades especiales")
				.version("1.0.0")
				.contact(new Contact()
					.name("EnlazadosTW Team")
					.email("info@enlazadostw.com")
					.url("https://enlazadostw.com")
				)
			)
			.addSecurityItem(new SecurityRequirement().addList("Bearer JWT"))
			.components(new io.swagger.v3.oas.models.Components()
				.addSecuritySchemes("Bearer JWT",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("JWT Bearer Token para autenticación. " +
							"Obtén el token en POST /auth/login")
				)
			);
	}
}

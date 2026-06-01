package com.example.EnlazadosTW.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración para servir archivos estáticos (Swagger, webjars, etc).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Servir webjars (Bootstrap, jQuery, etc - usados por Swagger)
		registry.addResourceHandler("/webjars/**")
			.addResourceLocations("classpath:/META-INF/resources/webjars/");

		// Dejar que springdoc-openapi maneje los recursos de Swagger automáticamente
		// NO mapear /swagger-ui/** aquí para evitar duplicación de rutas
	}
}


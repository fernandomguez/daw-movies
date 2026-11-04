package com.daw.movies.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Datos generales del contrato OpenAPI que springdoc genera desde el código.
 * <p>
 * Vive en {@code config}, fuera de {@code controller}, {@code domain} y
 * {@code persistence}: describe la aplicación entera, no una capa. Así habrá un
 * único Swagger UI cuando existan varios módulos.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI moviesOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Movies API")
						.version("0.0.1")
						.description("API REST de películas (proyecto de aprendizaje)."));
	}

}

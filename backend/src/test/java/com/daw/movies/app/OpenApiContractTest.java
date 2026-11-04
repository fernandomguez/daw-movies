package com.daw.movies.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprueba el contrato OpenAPI que springdoc genera a partir del código.
 * <p>
 * Es un test de integración: arranca toda la aplicación (necesita la base de
 * datos de Docker, como {@code MoviesApplicationTests}).
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiContractTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsListTheEndpointsOfBothModules() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				// catalog
				.andExpect(jsonPath("$.paths['/movies'].get").exists())
				.andExpect(jsonPath("$.paths['/movies'].post").exists())
				.andExpect(jsonPath("$.paths['/movies/{id}'].get").exists())
				.andExpect(jsonPath("$.paths['/movies/{id}'].put").exists())
				.andExpect(jsonPath("$.paths['/movies/{id}'].delete").exists())
				// ratings
				.andExpect(jsonPath("$.paths['/movies/{movieId}/ratings'].get").exists())
				.andExpect(jsonPath("$.paths['/movies/{movieId}/ratings'].post").exists())
				.andExpect(jsonPath("$.paths['/ratings/{id}'].delete").exists());
	}

	@Test
	void apiDocsCarryTheTitleOfTheApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Movies API"));
	}

	@Test
	void swaggerUiIsServed() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

}

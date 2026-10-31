package com.daw.movies;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.yaml.snakeyaml.Yaml;

/**
 * Comprueba el contrato de la fase 15A: que el YAML se sirve, que es YAML
 * válido y que describe los cuatro endpoints de la versión 1 (D-36).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiContractTests {

	private final HttpClient http = HttpClient.newHttpClient();

	@LocalServerPort
	private int port;

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
		return http.send(request, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void openApiContractIsServed() throws Exception {
		HttpResponse<String> response = get("/openapi.yaml");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("openapi: 3.0.3");
	}

	@Test
	void contractDescribesTheFourReadEndpointsOfVersionOne() throws Exception {
		Map<String, Object> contract = new Yaml().load(get("/openapi.yaml").body());

		@SuppressWarnings("unchecked")
		Map<String, Object> paths = (Map<String, Object>) contract.get("paths");

		assertThat(contract).containsEntry("openapi", "3.0.3");
		assertThat(paths).containsOnlyKeys(
				"/movies",
				"/movies/{id}",
				"/directors/{id}",
				"/directors/{id}/movies");
	}

	@Test
	void swaggerUiIsServed() throws Exception {
		HttpResponse<String> response = get("/swagger-ui/index.html");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("Swagger UI");
	}

}

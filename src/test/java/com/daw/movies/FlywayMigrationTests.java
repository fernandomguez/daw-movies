package com.daw.movies;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// Necesita MariaDB arrancado (docker compose up -d).
@SpringBootTest
class FlywayMigrationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayCreatesItsSchemaHistoryTableOnStartup() {
		// Flyway apunta en esta tabla cada migración que ejecuta; si existe, Flyway se ha ejecutado
		Integer tables = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables "
						+ "WHERE table_schema = 'movies' AND table_name = 'flyway_schema_history'",
				Integer.class);

		assertThat(tables).isEqualTo(1);
	}

}

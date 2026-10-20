package com.daw.movies;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// Necesita MariaDB arrancado (docker compose up -d).
@SpringBootTest
class DatabaseConnectionTests {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	private DataSource dataSource;

	@Test
	void connectsToMoviesDatabaseAsApplicationUser() throws SQLException {
		// try-with-resources: devuelve la conexión al pool al terminar
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();

			assertThat(metaData.getDatabaseProductName()).isEqualTo("MariaDB");
			assertThat(connection.getCatalog()).isEqualTo("movies");
			// Mínimo privilegio (D-17): la aplicación nunca se conecta como root
			assertThat(metaData.getUserName()).isEqualTo("movies_app");
		}
	}

	@Test
	void hibernateOnlyValidatesTheSchema() {
		// El esquema lo crea Flyway: Hibernate solo debe comprobarlo, nunca modificarlo (D-34)
		assertThat(entityManagerFactory.getProperties())
				.containsEntry("hibernate.hbm2ddl.auto", "validate");
	}

}

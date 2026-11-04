package com.daw.movies;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/*
 * Verificación de estructura de módulos (Spring Modulith): el módulo `Catalog`
 * está sellado (su subpaquete internal... en este caso todo el módulo no es
 * accesible desde fuera) y el grafo de dependencias no tiene ciclos. Si
 * `verify()` falla, se corrige el código o el contrato, nunca se relaja la
 * regla (error típico nº 10 del skill monolito-modular).
 */
class ModulithTest {

	ApplicationModules modules = ApplicationModules.of(MoviesApplication.class);

	@Test
	void verifyModuleBoundaries() {
		modules.verify();
	}

	@Test
	void generatesModuleDocumentation() {
		// Documentación de módulos en target/spring-modulith-docs (no se commitea)
		new Documenter(modules).writeDocumentation();
	}

}

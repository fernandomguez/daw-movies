package com.daw.movies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/*
 * `config` se declara módulo compartido: para Modulith, cada hijo directo del
 * paquete base es un módulo, y OpenApiConfig es transversal (toda la
 * aplicación), no un dominio de negocio (D-49). Con `catalog` anotado en su
 * package-info como @ApplicationModule, hoy solo hay un módulo real.
 */
@Modulithic(sharedModules = "config")
@SpringBootApplication
public class MoviesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviesApplication.class, args);
	}

}

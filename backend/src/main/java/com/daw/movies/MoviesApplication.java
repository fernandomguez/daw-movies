package com.daw.movies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * §3.1 de la arquitectura objetivo: @SpringBootApplication en el paquete base,
 * nada más. Los módulos (`catalog`, `ratings`) se declaran en su
 * `package-info.java` con @ApplicationModule, y `app` (la raíz de composición)
 * no es módulo porque solo los paquetes anotados se detectan como tales
 * (`spring.modulith.detection-strategy = explicitly-annotated`).
 */
@SpringBootApplication
public class MoviesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviesApplication.class, args);
	}

}

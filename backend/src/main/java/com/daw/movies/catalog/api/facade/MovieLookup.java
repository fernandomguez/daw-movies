package com.daw.movies.catalog.api.facade;

/**
 * DTO de la fachada: la porción de película que otros módulos pueden ver.
 * <p>
 * Record inmutable sin comportamiento ni anotaciones: cruza el borde del
 * módulo tal cual (D-50).
 */
public record MovieLookup(Long id, String title) {
}

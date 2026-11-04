/**
 * Interfaz con nombre «facade» (§3.2/§3.3): lo ÚNICO expuesto del módulo
 * catalog a los demás módulos. Solo la interfaz y sus records DTO viven aquí;
 * la implementación en {@code api.facade.impl} no forma parte de la interfaz
 * con nombre. Los consumidores se declaran como
 * {@code allowedDependencies = "catalog :: facade"}.
 */
@org.springframework.modulith.NamedInterface("facade")
package com.daw.movies.catalog.api.facade;

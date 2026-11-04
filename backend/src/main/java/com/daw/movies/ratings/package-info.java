/**
 * Módulo de aplicación `Ratings` (D-50): valoraciones (1–10) sobre las
 * películas del módulo {@code Catalog}.
 * <p>
 * Es el segundo módulo del monolito modular y el primero que consume una
 * fachada: {@code allowedDependencies = "catalog"} significa que lo único
 * tocable del exterior es la raíz de {@code com.daw.movies.catalog}, donde
 * vive {@code CatalogFacade} con su DTO {@code MovieLookup}. Toda referencia
 * a la película va <strong>por id</strong> (D-45).
 */
@org.springframework.modulith.ApplicationModule(
		displayName = "Ratings",
		allowedDependencies = "catalog"
)
package com.daw.movies.ratings;

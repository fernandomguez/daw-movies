/**
 * Módulo de aplicación `Ratings` (D-50): valoraciones (1–10) sobre las
 * películas del módulo {@code Catalog}.
 * <p>
 * Solo puede depender de la facade con nombre del módulo
 * {@code catalog} ({@code allowedDependencies = "catalog :: facade"}),
 * nunca de sus internals. La consulta de películas entra por el puerto
 * {@code AvailableMoviesPort} (declarado por este módulo en su
 * {@code internal.domain.port}) y el adaptador que llama a la fachada ajena
 * vive en {@code internal.persistence} — el único sitio que importa la
 * fachada de otro módulo (§3.3).
 */
@org.springframework.modulith.ApplicationModule(
		displayName = "Ratings",
		allowedDependencies = "catalog :: facade"
)
package com.daw.movies.ratings;

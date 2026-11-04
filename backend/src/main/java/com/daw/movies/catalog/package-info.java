/**
 * Módulo de aplicación `Catalog` (Spring Modulith).
 * <p>
 * Todo lo que cuelga de aquí (controller, domain, persistence) es interno:
 * la raíz del módulo no declara clases públicas, así que ningún otro módulo
 * puede usar nada de este. Tampoco consume a nadie: el contrato con otros
 * módulos (stock, order...) se escribirá en {@code allowedDependencies} cuando
 * existan, siguiendo la arquitectura objetivo (docs/arquitectura-monolito.md).
 */
@org.springframework.modulith.ApplicationModule(
		displayName = "Catalog"
)
package com.daw.movies.catalog;

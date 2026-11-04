package com.daw.movies;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/*
 * Suite de arquitectura (MOD-R10): las reglas de dependencia entre capas como
 * tests, para que romperlas rompa la compilación de la suite. Escrita ANTES de
 * la fase 4 (modularización) para que las reglas empiecen a guardar la casa
 * que ya está construida (docs/arquitectura.md, sección 3) y sigan funcionando
 * cuando el código se mueva al módulo `catalog`.
 *
 * Los paquetes se escriben con comodín (..domain..) para que la regla valga
 * igual dentro de cualquier módulo futuro. Los tests no se analizan (MOD-R10).
 */
@AnalyzeClasses(packages = "com.daw.movies", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

	/** MOD-R02: domain es el núcleo; no importa nada de controller ni de persistence. */
	@ArchTest
	static final ArchRule domainNoDependeDeNadie = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..controller..", "..persistence..");

	/** MOD-R01: controller solo usa interfaces; ni la impl. del servicio ni persistence. */
	@ArchTest
	static final ArchRule controllerSoloUsaInterfacesDeDomain = noClasses()
			.that().resideInAPackage("..controller..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..domain.service.impl..", "..persistence..");

	/** MOD-R03: el servicio usa el puerto MovieRepository; ninguna clase de domain llega a un Impl. */
	@ArchTest
	static final ArchRule domainNoConoceLosImpl = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat()
			.haveSimpleNameEndingWith("Impl");

	/** MOD-R04: el adaptador del repositorio usa la interfaz MovieDao, nunca MovieDaoJdbc. */
	@ArchTest
	static final ArchRule persistenceNoConoceLosImpl = noClasses()
			.that().resideInAPackage("..persistence..")
			.should().dependOnClassesThat()
			.haveSimpleNameEndingWith("Impl");

	/** MOD-R05: los DTO viven solo en controller; domain y persistence ni los ven. */
	@ArchTest
	static final ArchRule dtoSoloEnController = noClasses()
			.that().resideInAnyPackage("..domain..", "..persistence..")
			.should().dependOnClassesThat()
			.resideInAPackage("..controller.dto..");

	/** D-50: un módulo consume otro solo por su fachada; nada de sus internals. */
	@ArchTest
	static final ArchRule losModulosSoloTocanLaFachadaAjena = noClasses()
			.that().resideInAPackage("..ratings..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..catalog.controller..", "..catalog.domain..", "..catalog.persistence..");

	/** D-50: el grafo de dependencias es direccional; catalog no conoce a ratings. */
	@ArchTest
	static final ArchRule catalogNoConoceARatings = noClasses()
			.that().resideInAPackage("..catalog..")
			.should().dependOnClassesThat()
			.resideInAPackage("..ratings..");

}

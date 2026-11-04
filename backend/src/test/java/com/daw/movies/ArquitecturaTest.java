package com.daw.movies;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/*
 * LayerRulesTest equivalente (§3.7 de la arquitectura objetivo): las reglas de
 * dependencia de la estructura {modulo}/api + internal como tests. Los
 * paquetes van con comodín para valer en los dos módulos (catalog, ratings) y
 * en los que vengan. Los tests no se analizan (MOD-R10). Si una regla falla,
 * se corrige el código, nunca se relaja la regla (§3.10).
 */
@AnalyzeClasses(packages = "com.daw.movies", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

	/** §3.3: domain es puro — ni frameworks, ni api, ni hermanos del módulo. */
	@ArchTest
	static final ArchRule domainPuro = noClasses()
			.that().resideInAPackage("..internal.domain..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..api..", "..internal.application..", "..internal.persistence..", "org.springframework..");

	/** §3.3 + MOD-R03: el dominio (y la aplicación) usan interfaces; nunca los Impl. */
	@ArchTest
	static final ArchRule domainNoConoceLosImpl = noClasses()
			.that().resideInAPackage("..internal.domain..")
			.should().dependOnClassesThat()
			.haveSimpleNameEndingWith("Impl");

	/** §3.3 + MOD-R04: el adaptador del repositorio usa la interfaz del DAO, no su impl. */
	@ArchTest
	static final ArchRule persistenceNoConoceLosImpl = noClasses()
			.that().resideInAPackage("..internal.persistence..")
			.should().dependOnClassesThat()
			.haveSimpleNameEndingWith("Impl");

	/** §3.3: los casos de uso no tocan persistence ni api. */
	@ArchTest
	static final ArchRule applicationNoPersisteNiApi = noClasses()
			.that().resideInAPackage("..internal.application..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..internal.persistence..", "..api..");

	/** §3.3: el contrato HTTP no toca persistence ni la impl. de la fachada. */
	@ArchTest
	static final ArchRule httpNoPersisteNiImpl = noClasses()
			.that().resideInAPackage("..api.http..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..internal.persistence..", "..api.facade.impl..");

	/** §3.4: la fachada (sin impl) solo expone; no toca el núcleo interno. */
	@ArchTest
	static final ArchRule facadeSinTocarInternal = noClasses()
			.that().resideInAPackage("..api.facade")
			.should().dependOnClassesThat()
			.resideInAPackage("..internal..");

	/** §3.3: el adaptador de persistencia no ve el contrato HTTP ni la impl. de la fachada (ver fachada ajena sí: api.facade). */
	@ArchTest
	static final ArchRule persistenceNoTocaHttpNiFacadeImpl = noClasses()
			.that().resideInAPackage("..internal.persistence..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..api.http..", "..api.facade.impl..");

	/** §3.4: un módulo consume a otro solo por su interfaz con nombre; nada de internals ni impl. */
	@ArchTest
	static final ArchRule losModulosSoloTocanLaFachadaAjena = noClasses()
			.that().resideInAPackage("..ratings..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..catalog.api.http..", "..catalog.api.facade.impl..", "..catalog.internal..");

	/** §3.4: el grafo es acíclico y direccional; catalog no conoce a ratings. */
	@ArchTest
	static final ArchRule catalogNoConoceARatings = noClasses()
			.that().resideInAPackage("..catalog..")
			.should().dependOnClassesThat()
			.resideInAPackage("..ratings..");

}

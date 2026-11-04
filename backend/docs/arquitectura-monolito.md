# Arquitectura objetivo: monolito modular con Spring Modulith

> **La estructura de esta referencia ES ya la del código (D-51):** `api/facade(+impl)`,
> `api/http` e `internal/{application,domain,persistence}` están instalados en los
> dos módulos. Lo que queda de esta referencia es **técnico** y llega con el
> temario: JPA en `internal/persistence`, modelos enriquecidos, MapStruct y los
> `@ApplicationModuleTest` (§2 de abajo).
>
> La arquitectura **real** de hoy está en [arquitectura.md](arquitectura.md)
> ( Decisiones: [D-48](guia-estudio/decisiones-tecnicas.md#d-48-arquitectura-por-capas-con-dao-jdbc-y-dominio-con-tipos-simples),
> [D-49](guia-estudio/decisiones-tecnicas.md#d-49-monolito-modular-módulo-catalog-con-spring-modulith),
> [D-50](guia-estudio/decisiones-tecnicas.md#d-50-segundo-módulo-ratings-conectado-por-fachada) y
> [D-51](guia-estudio/decisiones-tecnicas.md#d-51-estructura-objetivo-en-los-dos-módulos-sync-con-el-2-del-skill) ). Código en inglés; documentación en español.

---

## 1. Lo que ya está instalado (D-49/D-50/**D-51**, 2026-11-04)

| Pieza del skill | Estado hoy |
|---|---|
| Módulos de negocio, paquetes en minúsculas y plural | ✅ `catalog` y `ratings` |
| `Application.java` en el paquete base | ✅ `MoviesApplication` (`@SpringBootApplication` y nada más) |
| `app` como raíz de composición, fuera de los módulos | ✅ `com.daw.movies.app.OpenApiConfig`; solo paquetes anotados son módulos (`detection-strategy = explicitly-annotated`) |
| **`api/facade(+impl)` · `api/http` · `internal/{application,domain,persistence}`** (§2 completo) | ✅ en los dos módulos (D-51) |
| `package-info.java` con `@ApplicationModule` + `allowedDependencies = "catalog :: facade"` | ✅ en ambos (la dependencia queda restringida a la interfaz con nombre) |
| `@NamedInterface("facade")` solo en `api/facade` | ✅ en `catalog`; **no** en `ratings` a propósito (§3.1: no se inventa fachada sin consumidor; la estrenará con el módulo 3) |
| Casos de uso `{Accion}{Entidad}UseCase.execute()` | ✅ 5 en catalog + 3 en ratings; `@Named` + `@jakarta.transaction.Transactional` los que escriben; ninguno invoca otro. Los de ratings llaman directo a sus puertos (§3.3 permite servicios **y/o** puertos): el `domain.service` nacería con la fachada de ratings, que no existe aún |
| Dominio puro de Spring (solo `@Named` tolerado) | ✅ la impureza `@Service`/`@Transactional` de D-48 murió con D-51 |
| Contratos con otros módulos declarados por el consumidor (puerto + adapter) | ✅ `AvailableMoviesPort` en `internal.domain.port` + `CatalogMoviesAdapter` en `internal.persistence.adapter` (únic import de la fachada ajena) |
| Handlers por módulo | ✅ `CatalogExceptionHandler`, `RatingExceptionHandler` |
| Verificación Modulith (`verify()`), grafo acíclico | ✅ `ModulithTest` (cogió en rojo la fachada sin `@NamedInterface` durante la migración) |
| `Documenter` (doc de módulos) | ✅ pinta `module-catalog` y `module-ratings` |
| ArchUnit `LayerRulesTest` equivalente (§3.7) | ✅ `ArquitecturaTest` (9 reglas) |
| DTO web separados de los de fachada | ✅ por estructura |

**No aplicable todavía, a propósito (piezas técnicas, no estructura):** JPA
(entidades, `JpaDao`, `PersistenceMapper`) — JDBC sigue; **MapStruct** — los
mappers van a mano (`CatalogFacadeMapper`, cero lógica); modelos enriquecidos + value objects; `@ApplicationModuleTest` por módulo.

## 2. Lo que queda encolado, y qué lo desbloquea

| Pieza de la arquitectura objetivo | Temario / condición |
|---|---|
| `internal/persistence` con `@Entity` + `JpaRepository` + `PersistenceMapper` | Tema 05 del profesor (JPA); la primera vez ya existe de referencia en la rama `jpa-fase-11-15a` |
| MapStruct (JSR-330) para `FacadeMapper` y `PersistenceMapper` | Con la migración JPA (los mappers a mano están en su jobs y son triviales) |
| Modelos enriquecidos + *value objects* (`internal/domain`) | Vuelve el comportamiento del dominio (tema 03; los VOs ya se hicieron una vez y se revocaron, D-46) |
| Tests `@ApplicationModuleTest` por módulo (§3.7.4) | Deuda reconocida en D-51: el arranque parcial de un módulo con BD real necesita diseñarse (portunas DataSource, fachada del módulo dependiente…) |
| Dominio 100 % puro (adiós al `jakarta.inject.Named`) | Se cablea con `@Bean` en `app` si el profesor lo quiere estricto |

Riesgo ya apuntado (D-45): si `Director`/`Actor` se partiesen en otro módulo, las relaciones entre módulos van **por id**, no por objeto — el skill fija cómo (puerto declarado por el consumidor + `{Capacidad}Adapter` solo en `persistence`).

---

## 3. La referencia completa (skill `monolito-modular`, volcada intocable)

Skill genérico: sirve para cualquier dominio y cualquier número de módulos. Los
nombres entre llaves son marcadores: `{base}` paquete base, `{modulo}` módulo
(minúsculas, plural de negocio), `{Modulo}` en PascalCase, `{Entidad}` un modelo,
`{Accion}` un verbo (`Crear`, `Cancelar`).

Úsalo para: crear un proyecto desde cero, añadir un módulo, añadir piezas a un
módulo (operación de fachada, caso de uso, endpoint, repositorio, adaptador) o
revisar si un proyecto respeta esta estructura.

### 3.1 Datos de entrada

Antes de generar, obtener (preguntar solo lo crítico; declarar lo asumido):

1. Paquete base `{base}` y nombre del artefacto.
2. Lista de módulos y, por cada uno:
   - modelos principales y sus invariantes,
   - operaciones que ofrece a OTROS módulos (contrato de fachada),
   - endpoints HTTP que expone a clientes externos,
   - módulos de los que depende y para qué.
3. Versión de Spring Boot y build (Maven por defecto), base de datos.

Validación previa:

- El grafo de dependencias entre módulos debe ser **acíclico**. Con ciclo (A→B y B→A) NO se genera: se explica y se propone salida (invertir la dependencia con un contrato declarado por el consumidor, publicar un evento, o fusionar los módulos).
- Un módulo que no ofrece nada a otros no necesita operaciones en su fachada: interfaz vacía o nada, pero sin inventar.
- Si un «módulo» no tiene modelos ni reglas (solo reenvía datos), probablemente no es un módulo.

### 3.2 Estructura

Sin paquete `infrastructure`: va aparte a propósito (todo lo técnico cae en `internal/persistence`, ver el bloque del mismo punto).

```text
{base}
├── Application.java                (raíz de la aplicación)
├── app                             (raíz de composición: configuración y cableado)
├── {modulo}                        (paquete raíz del módulo)
│   ├── package-info.java           (modelo de módulo: dependencias e interfaz expuesta)
│   ├── api
│   │   ├── facade                  (API pública: interfaz y DTO del contrato)
│   │   │   └── impl                (implementación de la fachada)
│   │   └── http                    (contrato HTTP: controllers y DTO web)
│   └── internal
│       ├── application             (casos de uso)
│       ├── domain                  (modelos, servicios, excepciones, interfaz de repositorio)
│       └── persistence             (entidades, DAO, repositorio e implementación, mapeo)
└── {otroModulo}                    (misma estructura)
```

Cada paquete hijo directo de `{base}` (salvo `app`) es un módulo para Spring Modulith.

### 3.3 Reglas por paquete

- **`Application.java`** — `@SpringBootApplication` en `{base}`. Nada más.
- **`app`** (raíz de composición) — `@Configuration` transversal: seguridad, OpenAPI, Jackson, CORS, beans explícitos ante ambigüedad. Única pieza que ve todos los módulos. **Prohibida la lógica de negocio.** Una clase `{Modulo}ModuleConfig` por módulo si hace falta cableado explícito.
- **`{modulo}/package-info.java`**

  ```java
  @org.springframework.modulith.ApplicationModule(
      displayName = "{Modulo}",
      allowedDependencies = { "{otroModulo} :: facade" }   // vacío si no depende de nadie
  )
  package {base}.{modulo};
  ```

  Siempre la forma `modulo :: facade`, que la dependencia quede restringida a la interfaz con nombre.
- **`api/facade`** — contrato para otros módulos. `package-info.java` con `@org.springframework.modulith.NamedInterface("facade")`. Contiene SOLO `public interface {Modulo}Facade` y `record` DTO (`{Accion}{Entidad}Dto`, `{Entidad}SummaryDto`...). Records inmutables, sin comportamiento, sin anotaciones Spring/JPA/validación, sin tipos de `internal`. Nunca dominio ni entidades. Se diseña por lo que los consumidores necesitan.
- **`api/facade/impl`** — `{Modulo}FacadeImpl implements {Modulo}Facade` con `@jakarta.inject.Named`. Solo delega en servicios de dominio y mapea dominio ↔ DTO (mapper MapStruct `{Modulo}FacadeMapper`). Sin reglas de negocio, sin repositorios, **sin invocar casos de uso**. No lleva `@NamedInterface`: fuera no tocable.
- **`api/http`** — `@RestController` `{Entidad}Controller`; DTO web `{Accion}{Entidad}Request` / `{Entidad}Response` propios, distintos de los de fachada aunque coincidan campo a campo. Validación `jakarta.validation` en los Request. `@RestControllerAdvice` `{Modulo}ExceptionHandler` (excepciones de dominio → HTTP). Los controllers llaman a **casos de uso** del propio módulo; nunca a repositorios, a `persistence` ni a fachadas ajenas. No es interfaz con nombre.
- **`internal/application`** — casos de uso: `{Accion}{Entidad}UseCase` con un único método público `execute(...)`, `@jakarta.inject.Named`; `@jakarta.transaction.Transactional` si escribe. Coordina: invoca servicios de dominio y puertos. **No invoca otros casos de uso** (regla compartida → servicio de dominio). Sin Spring Web ni `persistence`.
- **`internal/domain`** — núcleo: modelos enriquecidos (estado + comportamiento + invariantes), *value objects*, enums; servicios de dominio `{Entidad}Service` + `{Entidad}ServiceImpl` si la regla cruza modelos; excepciones `{Algo}Exception`; **puertos**: `{Entidad}Repository`, contratos con otros módulos declarados por el consumidor (`{Capacidad}Port`, con records propios; nunca importar DTO del proveedor) y puertos técnicos (`PasswordHasher`, `TokenProvider`...). **Java puro**: prohibido depender de `org.springframework..`, `jakarta.persistence..`, `jakarta.validation..`, `org.hibernate..`, `org.mapstruct..`. Se tolera `jakarta.inject.Named` en servicios si se cablean por escaneo; dominio 100 % puro ⇒ cablear sus servicios con `@Bean` en `app`.
- **`internal/persistence`** — adaptadores de salida (sin `infrastructure`): `{Entidad}Entity` (`@Entity`), `{Entidad}JpaDao extends JpaRepository<...>`, `{Entidad}RepositoryImpl implements {Entidad}Repository`, `{Entidad}PersistenceMapper` (MapStruct). `{Capacidad}Adapter implements {Capacidad}Port` (llama a `{OtroModulo}Facade` y traduce a records del puerto): **único sitio del módulo que importa una fachada ajena**. Adaptadores técnicos (`BCryptPasswordHasher`, `JwtTokenProvider`, clientes HTTP). Sufijos claros (`RepositoryImpl`, `Adapter`, `Client`); clases de implementación con visibilidad de paquete cuando se pueda.

### 3.4 Dirección de dependencias

```text
api/http ──────► internal/application ──► internal/domain ◄── internal/persistence ──► {otro}.api.facade

api/facade/impl ───────────────────────► internal/domain
api/facade/impl ──► api/facade
app ──► todo
```

Prohibido:

- `domain` → cualquier otro paquete del módulo o de otro módulo.
- `application` → `persistence`, `api.*`.
- `api.http` → `persistence`, `api.facade.impl`, otros módulos.
- `api.facade` → `internal`, `impl`.
- Cualquier módulo → `internal`, `api.facade.impl` o `api.http` de otro módulo.

### 3.5 Cableado

Clases internas y `FacadeImpl` con `@jakarta.inject.Named` + inyección por constructor (sin `@Autowired`). MapStruct en modo JSR-330 (`-Amapstruct.defaultComponentModel=jsr330`). Spring escanea desde `{base}`.

### 3.6 Dependencias Maven

**Verificar en la doc oficial la versión de Spring Modulith compatible con el Boot del proyecto antes de fijarla** (nunca de memoria). Y: `spring-boot-starter-web`, `-validation`, `-data-jpa`, driver BD; `jakarta.inject:jakarta.inject-api`; `org.mapstruct:mapstruct` + processor en `annotationProcessorPaths` (con `lombok-mapstruct-binding` si hay Lombok); `spring-modulith-bom` en `dependencyManagement` + `-starter-core`; `-starter-test` (test); `com.tngtech.archunit:archunit-junit5` (test).

### 3.7 Pruebas que siempre se generan

1. `ModularityTest` (aquí: `ModulithTest`) — `ApplicationModules.of(Application.class)`; `verify()` y `Documenter`.
2. `LayerRulesTest` (ArchUnit): `..internal.domain..` puro de framework; `..internal.application..` sin `persistence` ni `api..`; `..api.http..` sin `persistence` ni `api.facade.impl..`; `..api.facade..` sin `internal..` y solo interfaces/records; `..api.facade.impl..` sin `internal.application..` ni `internal.persistence..`; nadie externo entra en `internal`, `api.http` ni `api.facade.impl` (lo cubre `verify()`).
3. Por caso de uso: unitario con Mockito (a quién llama, con qué datos).
4. Por módulo: `@ApplicationModuleTest` (con dependencias directas).
5. Unitarios de invariantes de los modelos enriqueidos.

### 3.8 Procedimiento de generación

1. Validar entradas y grafo (3.1). 2. `pom.xml`. 3. `Application.java` y `app`. 4. Por módulo: `package-info` → `api/facade` → `internal/domain` → `internal/persistence` → `internal/application` → `api/facade/impl` → `api/http`. 5. Adaptadores entre módulos (puerto del consumidor + adapter en su `persistence`). 6. Pruebas (3.7). 7. `mvn test`; si `verify()` o ArchUnit fallan, **se corrige el código, nunca se relaja la regla**.

Para añadir una operación consumible: método en `{Modulo}Facade` + DTO → lógica en servicio de dominio → delegación y mapeo en `FacadeImpl` → `allowedDependencies` del consumidor → puerto + adapter en el consumidor.

### 3.9 Checklist de revisión

- [ ] Grafo de módulos acíclico y `allowedDependencies` con `modulo :: facade`.
- [ ] Solo `api/facade` tiene `@NamedInterface`.
- [ ] Fachada solo con interfaz y records; ningún tipo de dominio cruza el límite.
- [ ] `FacadeImpl` sin reglas propias y sin llamar a casos de uso.
- [ ] Controllers solo con casos de uso; DTO web separados de los de fachada.
- [ ] Ningún caso de uso llama a otro.
- [ ] Dominio sin dependencias de framework.
- [ ] Contratos con otros módulos declarados por el consumidor en su `domain`; el único import de otra fachada está en `persistence`.
- [ ] No existe `infrastructure`; pieza técnica con sufijo en `persistence`.
- [ ] `app` sin lógica de negocio.
- [ ] `ModularityTest`, `LayerRulesTest`, tests de casos de uso y `@ApplicationModuleTest` en verde.

### 3.10 Errores típicos a señalar sin rodeos

- Fachadas que exponen entidades o modelos de dominio.
- `FacadeImpl` convertido en segundo servicio con reglas.
- Un módulo importando DTO ajeno dentro de su `domain` o `application`.
- Controllers que llaman a repositorios o fachadas de otros módulos.
- `@Service`/`@Autowired` repartidos por `domain`.
- Módulos sin modelos ni reglas (capas disfrazadas de módulo).
- «Arreglar» un fallo de `verify()` ampliando `allowedDependencies` o quitando reglas.

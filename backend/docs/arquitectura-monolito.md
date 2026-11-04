# Arquitectura objetivo: monolito modular con Spring Modulith

> **Esta es la arquitectura de llegada, no la del código de hoy.** La describe el
> skill `monolito-modular` (apuntes del profesor) y se irá adoptando conforme el
> temario la permita: su pieza mayor (JPA, `internal/persistence` con entidades)
> llega cuando el profesor dé el tema, «no lo hemos hecho todavía», y el resto
> (fachadas, casos de uso) cuando haya ≥2 módulos de negocio.
>
> La arquitectura **real** de hoy está en [arquitectura.md](arquitectura.md)
> ( Decisiones: [D-48](guia-estudio/decisiones-tecnicas.md#d-48-arquitectura-por-capas-con-dao-jdbc-y-dominio-con-tipos-simples) y
> [D-49](guia-estudio/decisiones-tecnicas.md#d-49-monolito-modular-módulo-catalog-con-spring-modulith) ). Código en inglés; documentación en español.

---

## 1. Lo que ya está instalado (fase 4, D-49, 2026-11-04)

| Pieza del skill | Estado hoy |
|---|---|
| Un módulo de negocio, paquete en minúsculas y plural | ✅ `com.daw.movies.catalog` |
| `Application.java` en el paquete base | ✅ `MoviesApplication`, con `@Modulithic(sharedModules = "config")` |
| Configuración transversal fuera de los módulos (el `app` del skill) | ✅ `com.daw.movies.config.OpenApiConfig` (módulo compartido) |
| `package-info.java` con `@ApplicationModule` | ✅ en `catalog` (sellado, `displayName = "Catalog"`, sin `allowedDependencies`: no ofrece ni consume) |
| Verificación Modulith (`verify()`), grafo acíclico | ✅ `ModulithTest` |
| `Documenter` (doc de módulos en `target/spring-modulith-docs`) | ✅ en `ModulithTest` |
| ArchUnit como segunda barrera | ✅ `ArquitecturaTest` (5 reglas con comodines, MOD-R01–R05) |

**No aplicable todavía, a propósito:** `api/facade` (nadie consume el módulo; el
propio skill §1 lo dice: no se inventa fachada), `internal/…` (la estructura
planas de D-48 vive igual de bien dentro del módulo; se reordena al migrar a
la arquitectura objetivo), JPA/MapStruct (todo JDBC, D-48).

## 2. Lo que queda encolado, y qué lo desbloquea

| Pieza de la arquitectura objetivo | Temario / condición |
|---|---|
| `internal/persistence` con `@Entity` + `JpaRepository` + mapper entidad↔dominio | Tema 05 del profesor (JPA); la primera vez ya existe de referencia en la rama `jpa-fase-11-15a` |
| Modelos enriquecidos + *value objects* (`internal/domain`) | Vuelve el comportamiento del dominio (tema 03; los VOs ya se hicieron una vez y se revocaron, D-46) |
| **Casos de uso** `{Accion}{Entidad}UseCase.execute` (en vez de `MovieService` con 5 métodos) | Con la arquitectura rica; y el skill prohíbe llamar de un caso de uso a otro |
| `api/facade` + `@NamedInterface("facade")` + DTO-records de fachada | Cuando exista un **segundo** módulo (`stock`, `order`...) que lo consuma |
| Dominio 100 % puro (adiós a `@Service`/`@Transactional` en `domain`, **hoy impureza aceptada de D-48**) | Misma migración JPA: se cablea con `@Bean` en `app` o se cambia a `jakarta.inject.Named` |
| `@ApplicationModuleTest` por módulo; `allowedDependencies = "{otroModulo} :: facade"` | Con ≥2 módulos |

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

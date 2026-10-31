# Tema 07 — Conexión con la base de datos: JDBC, driver y `DataSource`

> Fase del plan: 7. Lo que hicimos: [07-conexion-java-mariadb.md](../07-conexion-java-mariadb.md).

## 1. El problema

Java y MariaDB son programas distintos. MariaDB escucha en un puerto de red (en nuestro caso, el 3307 de Windows, que lleva al 3306 del contenedor) y habla su propio protocolo. Java necesita una forma estándar de enviarle SQL y recibir resultados.

## 2. JDBC: la interfaz estándar

**JDBC** (*Java Database Connectivity*) es la API estándar de Java para bases de datos relacionales. Define **interfaces** (`Connection`, `PreparedStatement`, `ResultSet`...), pero no sabe hablar con ninguna base de datos concreta.

Es la misma idea que el puerto y el adaptador de la fase 5: JDBC es el "contrato" y cada fabricante aporta la implementación.

## 3. El driver: la implementación del fabricante

El **driver JDBC** implementa esas interfaces para una base de datos concreta. Para MariaDB es `org.mariadb.jdbc:mariadb-java-client`.

- Va con **`scope runtime`**: nuestro código nunca escribe `import org.mariadb...`; solo hace falta cuando el programa se ejecuta.
- Se elige a partir de la **URL JDBC**: `jdbc:mariadb://localhost:3307/movies`. El prefijo `jdbc:mariadb:` indica qué driver usar.

```
jdbc:mariadb://localhost:3307/movies
└┬─┘ └──┬──┘   └───┬───┘ └┬─┘ └─┬──┘
 │      │          │      │     └─ base de datos
 │      │          │      └─ puerto (el publicado por Docker)
 │      │          └─ máquina
 │      └─ qué driver
 └─ protocolo JDBC
```

## 4. `DataSource` y el *pool* de conexiones

Abrir una conexión con la base de datos es lento (red, autenticación...). Por eso no se abre una conexión por consulta, sino que se usa un **pool**: un grupo de conexiones abiertas que se prestan y se devuelven.

- **`DataSource`** es la interfaz estándar (de `javax.sql`) de "algo que me da conexiones".
- **HikariCP** es la implementación que usa Spring Boot por defecto: un *pool* rápido.

```
Hibernate / Spring Data  ──pide conexión──▶  DataSource (HikariCP)
                                                │  pool: [c1][c2][c3]...
                                                ▼
                                     driver mariadb-java-client
                                                │  protocolo MariaDB
                                                ▼
                                   MariaDB en Docker (localhost:3307)
```

## 5. Quién crea el `DataSource`: la autoconfiguración

Nosotros no escribimos `new HikariDataSource(...)`. Spring Boot lo hace solo cuando:

1. Encuentra en el proyecto las clases de JDBC y de Hikari (las trae `spring-boot-starter-jdbc`, incluido en `spring-boot-starter-data-jpa`).
2. Tiene los datos de conexión: `spring.datasource.url`, `username` y `password`.

Si se cumple lo primero pero no lo segundo, **la aplicación no arranca**. Es lo que vimos en el paso 7.1 (`'url' attribute is not specified`).

## 6. Las capas de la persistencia, de arriba abajo

| Pieza | Qué es | Dónde la veremos |
|---|---|---|
| Spring Data JPA | Genera repositorios a partir de interfaces | Fase 12 |
| JPA (Jakarta Persistence 3.2) | Especificación: anotaciones y `EntityManager` | Fase 11 |
| Hibernate 7.4 | Implementación de JPA: traduce objetos ↔ SQL | Fases 7 (`ddl-auto`) y 11 |
| `DataSource` (HikariCP 7.0) | Pool de conexiones | Fase 7 |
| JDBC + driver MariaDB 3.5 | Envía el SQL por la red | Fase 7 |

Cada capa usa la de debajo. Aunque casi nunca escribiremos JDBC a mano, **todo** acaba pasando por él.

## Preguntas de repaso

**¿Por qué el driver tiene `scope runtime` y no `compile`?**
Porque nuestro código solo usa las interfaces estándar (JDBC, JPA). El driver concreto solo se necesita al ejecutar. Así, cambiar de base de datos no obliga a cambiar el código.

**Si el driver de MariaDB está en el proyecto, ¿por qué Spring dice "Failed to determine a suitable driver class"?**
Porque elige el driver a partir de la URL, y la URL todavía no estaba configurada.

**¿Qué es un pool de conexiones y por qué se usa?**
Un grupo de conexiones ya abiertas que se reutilizan. Abrir una conexión es caro; reutilizarla, no.

## 7. Configuración externa: `application.yaml` y variables

Spring Boot reúne la configuración de muchas fuentes. Si una propiedad aparece en varias, gana la de mayor prioridad. De más a menos (simplificado):

1. Argumentos de línea de comandos (`--spring.datasource.url=...`).
2. **Variables de entorno del sistema operativo**.
3. Archivos importados con `spring.config.import` (nuestro `.env`) y `application.yaml`.

Por eso los marcadores `${MARIADB_USER}` se resuelven primero con una variable de entorno real y, si no existe, con el valor de `.env` (D-32).

| Sintaxis | Significado |
|---|---|
| `${MARIADB_USER}` | Obligatoria: si no existe, la aplicación no arranca |
| `${MARIADB_PORT:3307}` | Con valor por defecto: si no existe, usa `3307` |

**Regla:** en `application.yaml`, que sí está en Git, nunca hay secretos; solo marcadores.

## 8. El dialecto de Hibernate

Cada base de datos tiene pequeñas diferencias de SQL (tipos, paginación, funciones). El **dialecto** es la clase de Hibernate que conoce esas diferencias. Al arrancar, Hibernate pide a la base de datos su nombre y versión y elige el dialecto solo (`MariaDBDialect`). Por eso, si no hay conexión, falla con `Unable to determine Dialect without JDBC metadata`.

## 9. Los secretos también se escapan por los logs

En la fase 7 la contraseña no estaba en ningún archivo versionado y, aun así, apareció en el log de arranque: Hibernate escribe la URL que le devuelve el driver, que incluye las credenciales. Un log se copia en foros, chats e informes de CI, así que hay que revisar qué contiene. Lo resolvimos subiendo ese logger a `WARN` con `logging.level` (D-33).

**¿Cómo sabemos que se leyó `.env` si el test falló con Docker apagado?**
Porque falló al **conectar** (`Connection refused`), no al resolver `${MARIADB_USER}`. Si la variable no existiera, habría fallado antes.

## 10. `ddl-auto`: quién manda en el esquema

Hibernate sabe generar tablas a partir de las entidades (`spring.jpa.hibernate.ddl-auto`). Es útil para prototipos, pero en un proyecto real el esquema lo gestionan **migraciones versionadas** (Flyway, fase 8):

| | `ddl-auto: update` | Flyway + `ddl-auto: validate` |
|---|---|---|
| Quién escribe el SQL | Hibernate, al arrancar | Nosotros, en archivos `V1__...sql` |
| ¿Se revisa y se versiona? | No | Sí, en Git |
| Borrar o renombrar columnas | No sabe | Sí |
| Si entidad y tabla no encajan | Intenta cambiar la tabla | La aplicación **no arranca** y dice por qué |

Valor por defecto en Spring Boot: `create-drop` si la base de datos está en memoria (H2...), `none` en el resto. Por eso, sin configurarlo, Hibernate no recibía ninguna propiedad `hibernate.hbm2ddl.auto`.

## 11. Tests de integración con base de datos

`@SpringBootTest` arranca **toda** la aplicación, incluida la conexión. Es un test de integración: comprueba que las piezas funcionan juntas de verdad.

- Se pueden inyectar *beans* en el test con `@Autowired` (`DataSource`, `EntityManagerFactory`...).
- Spring **guarda el contexto en caché**: si varias clases de test tienen la misma configuración, se arranca una sola vez (por eso la segunda clase tarda milisegundos).
- Precio: el test depende de que MariaDB esté arrancado (D-35). En la fase 19, Testcontainers creará un MariaDB desechable para los tests.
- **Un test debe poder fallar.** Si no se ha visto nunca en rojo, hay que provocarlo (por ejemplo, con una contraseña incorrecta) para saber que comprueba algo.

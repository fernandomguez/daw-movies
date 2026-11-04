# 04 — Docker, Docker Compose y MariaDB

> **Conceptos:** [temario/04-docker-compose-mariadb.md](temario/04-docker-compose-mariadb.md).

## Objetivo

Levantar MariaDB 12.3 en un contenedor, independiente de la aplicación, y comprobar que funciona.

## Archivos creados

| Archivo | Responsabilidad | ¿En Git? |
|---|---|---|
| `compose.yaml` | Describe el servicio `mariadb`: imagen, variables, puerto, volumen, charset, healthcheck | Sí |
| `.env.example` | Plantilla de variables con valores falsos (`change_me`) | Sí |
| `.env` | Valores reales (contraseñas). Creado por el alumno | **No** |
| `.gitignore` | Excluye `.env` | Sí |

## Pasos y resultados reales

### 4.1 Docker funciona

```powershell
docker run --rm hello-world
```
Resultado: `Hello from Docker! This message shows that your installation appears to be working correctly.`

### 4.2 – 4.4 Arrancar MariaDB

```powershell
docker compose up -d
docker compose ps
```
Resultado:
```
NAME               IMAGE          STATUS                    PORTS
movies-mariadb-1   mariadb:12.3   Up 10 seconds (healthy)   0.0.0.0:3307->3306/tcp
```
Se crearon el volumen `movies_mariadb-data` y la red `movies_default`.

### 4.5 Conexión y comprobaciones

```powershell
$sql = @'
SELECT VERSION() AS version, CURRENT_USER() AS usuario, DATABASE() AS bd_actual;
SHOW DATABASES;
SELECT @@character_set_server AS charset, @@collation_server AS collation_srv, @@default_storage_engine AS motor;
'@
$sql | docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" -t movies'
```

| Comprobación | Resultado |
|---|---|
| Versión | `12.3.3-MariaDB-ubu2404` |
| Usuario | `movies_app@%` (no root) |
| Bases de datos visibles | `information_schema`, `movies` (mínimo privilegio) |
| Charset / collation del servidor y de `movies` | `utf8mb4` / `utf8mb4_uca1400_ai_ci` |
| Motor por defecto | `InnoDB` (admite claves foráneas) |

### 4.4 Persistencia con el volumen (prueba real)

1. Crear tabla temporal `volume_check` con los valores `Amenábar` y `Verdú`.
2. `docker compose down` → el contenedor y la red se borran; `docker volume ls` sigue mostrando `movies_mariadb-data`.
3. `docker compose up -d` → contenedor **nuevo** (hora de creación distinta).
4. `SELECT * FROM volume_check` → `Amenábar`, `Verdú`. **Los datos sobrevivieron** y las tildes se guardaron bien.
5. `DROP TABLE volume_check` → base de datos `movies` vacía, lista para Flyway (fase 8).

### 4.6 Puertos desde Windows

```powershell
Test-NetConnection -ComputerName localhost -Port 3307   # True → MariaDB en Docker
Test-NetConnection -ComputerName localhost -Port 3306   # True → MariaDB instalado en Windows
```

## Errores encontrados y cómo se resolvieron

### 1. Docker Desktop no estaba arrancado
- **Síntoma:** `failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine ... The system cannot find the file specified.`
- **Causa:** el comando `docker` está instalado, pero el motor (que vive en Docker Desktop) no estaba en ejecución.
- **Solución:** abrir Docker Desktop y esperar a que `docker info` responda.

### 2. Puerto 3306 ocupado
- **Síntoma:** al comprobar el puerto antes de arrancar, el 3306 estaba en uso por el proceso `mysqld`, que corresponde al **servicio de Windows `MariaDB`** (arranque automático).
- **Causa:** hay un MariaDB instalado localmente; dos programas no pueden escuchar en el mismo puerto.
- **Solución:** publicar el contenedor en el **3307** (`"3307:3306"`). No se tocó el servicio de Windows. Ver D-15.

### 3. Error de sintaxis SQL por las comillas
- **Síntoma:** `ERROR 1064 (42000) at line 1: You have an error in your SQL syntax ... near ''` y MariaDB solo había recibido `SELECT`.
- **Causa:** el SQL iba entre comillas dentro de otro comando entre comillas. Al pasar por PowerShell → Docker → `sh`, las comillas internas se perdieron y el texto llegó cortado.
- **Solución:** enviar el SQL por la **entrada estándar** (`$sql | docker compose exec -T ...`) en lugar de como argumento.

### 4. Claude Code no puede crear ni leer `.env*`
- **Causa:** una regla de permisos de Claude Code protege los archivos `.env*` (pueden contener secretos).
- **Solución:** el alumno creó `.env` y `.env.example` a mano. La regla se mantiene.

## Decisiones

En [decisiones-tecnicas.md](decisiones-tecnicas.md): D-15 (puerto 3307), D-16 (charset), D-17 (credenciales y usuario de la aplicación).

## Qué recordar

- Abrir **Docker Desktop** y ejecutar `docker compose up -d` antes de trabajar.
- La base de datos del proyecto está en **`localhost:3307`**.
- `docker compose down -v` **borra los datos**.

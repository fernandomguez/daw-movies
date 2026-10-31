# 04 — Docker, Docker Compose y MariaDB

> **Fase del plan:** 4 (Docker, Docker Compose y MariaDB).

## Objetivo

Tener MariaDB funcionando **dentro de un contenedor**, sin instalarlo en Windows, con las credenciales fuera del repositorio y con los datos a salvo aunque el contenedor se borre.

## 1. Qué es Docker

**Problema:** instalar MariaDB directamente en Windows deja el sistema "tocado" (servicios, puertos, versiones). Si otro compañero instala otra versión, el proyecto puede comportarse distinto en cada ordenador.

**Solución:** Docker ejecuta programas en **contenedores**: entornos aislados que llevan dentro todo lo que el programa necesita.

| Concepto | Qué es | Analogía | En nuestro proyecto |
|---|---|---|---|
| **Imagen** | Plantilla de solo lectura con el programa ya instalado | La receta (o el molde) | `mariadb:12.3` |
| **Contenedor** | Una ejecución de una imagen, con su propio estado | El pastel hecho con esa receta | `movies-mariadb-1` |
| **Etiqueta (*tag*)** | Versión concreta de una imagen | La edición de la receta | `12.3` |

Puedes crear y borrar contenedores tantas veces como quieras: la imagen no cambia.

Prueba de que Docker funciona:

```powershell
docker run --rm hello-world
# Hello from Docker!
# This message shows that your installation appears to be working correctly.
```

## 2. Qué es Docker Compose

Arrancar MariaDB solo con `docker run` exigiría un comando larguísimo (imagen, variables, puertos, volúmenes...) que habría que recordar y escribir siempre igual.

**Docker Compose** permite **describir los contenedores en un archivo** (`compose.yaml`) y arrancarlos con un solo comando. El archivo se guarda en el repositorio, así que cualquiera levanta exactamente la misma base de datos.

### Nuestro `compose.yaml`, parte por parte

```yaml
name: movies                      # Nombre del proyecto: prefijo de contenedores, redes y volúmenes

services:
  mariadb:                        # Nombre del servicio (lo usaremos en los comandos)
    image: mariadb:12.3           # Imagen y versión exacta (nunca "latest": cambiaría sin avisar)
    environment:                  # Variables que la imagen usa al crear la base de datos
      MARIADB_ROOT_PASSWORD: ${MARIADB_ROOT_PASSWORD}
      MARIADB_DATABASE: ${MARIADB_DATABASE}
      MARIADB_USER: ${MARIADB_USER}
      MARIADB_PASSWORD: ${MARIADB_PASSWORD}
    command:                      # Opciones extra para el servidor MariaDB
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_uca1400_ai_ci
    ports:
      - "${MARIADB_PORT:-3307}:3306"
    volumes:
      - mariadb-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 20s

volumes:
  mariadb-data:                   # Declaración del volumen con nombre
```

Las secciones siguientes explican cada bloque.

## 3. Variables de entorno: credenciales fuera del repositorio

`${MARIADB_PASSWORD}` no es la contraseña: es una **referencia** a una variable. Docker Compose busca su valor en el archivo **`.env`** de la misma carpeta.

| Archivo | Contenido | ¿Se sube a Git? |
|---|---|---|
| `compose.yaml` | La **estructura**, con referencias `${...}` | ✅ Sí |
| `.env` | Los **valores reales** (contraseñas) | ❌ No (está en `.gitignore`) |
| `.env.example` | La **plantilla** con valores falsos (`change_me`) | ✅ Sí |

Así, el repositorio explica **qué** hay que configurar, pero nunca contiene **los secretos**.

Qué hace cada variable (la imagen oficial las usa **solo la primera vez**, cuando el volumen está vacío):

| Variable | Efecto |
|---|---|
| `MARIADB_ROOT_PASSWORD` | Contraseña del administrador `root`. Solo para administración; la aplicación **no** la usará. |
| `MARIADB_DATABASE` | Crea la base de datos `movies`. |
| `MARIADB_USER` / `MARIADB_PASSWORD` | Crea el usuario `movies_app` con permisos **solo sobre `movies`**. |

**Principio de mínimo privilegio:** la aplicación se conecta con un usuario que solo puede tocar su base de datos. Por eso `movies_app` solo ve `information_schema` y `movies` al hacer `SHOW DATABASES`.

> ⚠️ Como estas variables solo se aplican la primera vez, **cambiar la contraseña en `.env` después no cambia la de la base de datos ya creada**. Habría que cambiarla con SQL o recrear el volumen.

## 4. Puertos: `"3307:3306"`

Un contenedor tiene su **propia red**. MariaDB escucha dentro del contenedor en el puerto 3306, pero desde Windows no se ve, salvo que **publiquemos** el puerto:

```
"PUERTO_EN_TU_ORDENADOR : PUERTO_DENTRO_DEL_CONTENEDOR"
        3307            :            3306
```

```
Tu Windows                                    Contenedor movies-mariadb-1
localhost:3306 → MariaDB instalado en Windows
localhost:3307 ──────────────────────────────► :3306 → MariaDB 12.3 (Docker)
```

**¿Por qué 3307 y no 3306?** En este ordenador hay un **MariaDB instalado como servicio de Windows** que ya ocupa el 3306. Dos programas no pueden escuchar en el mismo puerto. Usamos el 3307 por fuera; por dentro sigue siendo 3306.

> ⚠️ Consecuencia importante: si una aplicación se conecta a `localhost:3306`, hablará con el MariaDB de Windows, **no** con el del contenedor. Nuestra aplicación usará el **3307**.

`${MARIADB_PORT:-3307}` significa "usa la variable `MARIADB_PORT`; si no existe, usa 3307".

## 5. Volúmenes: que los datos sobrevivan

Un contenedor es **desechable**: al borrarlo, se pierde todo lo que había escrito dentro. MariaDB guarda sus datos en `/var/lib/mysql` (dentro del contenedor).

Un **volumen** es un espacio de almacenamiento gestionado por Docker, **fuera** del contenedor. Con `mariadb-data:/var/lib/mysql` le decimos: "lo que MariaDB escriba en `/var/lib/mysql`, guárdalo en el volumen `mariadb-data`".

```
Contenedor (desechable)                Volumen movies_mariadb-data (persistente)
/var/lib/mysql  ──────────────────────► datos de MariaDB
```

Lo comprobamos de verdad (ver [04-docker-y-mariadb.md](../04-docker-y-mariadb.md)): creamos una tabla, **borramos el contenedor** con `docker compose down`, creamos uno nuevo con `up` y la tabla seguía ahí.

| Comando | Contenedor | Volumen (datos) |
|---|---|---|
| `docker compose stop` | Se detiene | Se conserva |
| `docker compose down` | **Se borra** | Se conserva ✅ |
| `docker compose down -v` | Se borra | **Se borra** ⚠️ (base de datos vacía) |

`down -v` es útil para empezar de cero, pero **borra todos los datos**.

## 6. Juego de caracteres: `utf8mb4`

Un **juego de caracteres** (*charset*) define qué caracteres se pueden guardar. **`utf8mb4`** admite cualquier carácter Unicode: tildes (*Amenábar*), eñes, alfabetos no latinos y emojis.

La **intercalación** (*collation*) define cómo se **comparan y ordenan** los textos. `utf8mb4_uca1400_ai_ci` significa:
- `uca1400`: reglas de ordenación Unicode modernas;
- `ai` (*accent-insensitive*): al comparar, `Verdu` = `Verdú`;
- `ci` (*case-insensitive*): al comparar, `nolan` = `Nolan`.

Es la intercalación por defecto en las versiones actuales de MariaDB; la escribimos explícitamente para no depender de valores por defecto.

## 7. Healthcheck: saber cuándo está lista

"Contenedor arrancado" no significa "base de datos lista": MariaDB tarda unos segundos en inicializarse. El **healthcheck** es una comprobación que Docker ejecuta periódicamente:

- `healthcheck.sh` es un script incluido en la imagen oficial de MariaDB.
- `--connect`: comprueba que acepta conexiones.
- `--innodb_initialized`: comprueba que el motor InnoDB (el que usaremos) está listo.

Estados: `starting` → `healthy` (correcto) o `unhealthy` (falla). Se ve con `docker compose ps`.

## 8. Conectarse a MariaDB

La imagen incluye el cliente de línea de comandos `mariadb`. Lo ejecutamos **dentro del contenedor** con `docker compose exec`, usando las variables que el contenedor ya tiene, para que la contraseña no aparezca en pantalla:

```powershell
"SELECT VERSION();" | docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" -t movies'
```

| Parte | Significado |
|---|---|
| `"SELECT ...;" \|` | Envía el SQL por la entrada estándar del comando |
| `docker compose exec -T mariadb` | Ejecuta algo dentro del servicio `mariadb` (`-T`: sin terminal interactiva, necesario al usar `\|`) |
| `sh -c '...'` | Usa la consola del contenedor, donde existen `$MARIADB_USER` y `$MARIADB_PASSWORD` |
| `-t` | Muestra el resultado en forma de tabla |
| `movies` | Base de datos a usar |

Para una sesión **interactiva** (escribir consultas a mano):

```powershell
docker compose exec mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" movies'
# ... escribes SQL ... y sales con: exit
```

## 9. Comandos del día a día

| Comando | Para qué |
|---|---|
| `docker compose up -d` | Arrancar MariaDB (en segundo plano, `-d`) |
| `docker compose ps` | Ver el estado (debe poner `healthy`) |
| `docker compose logs mariadb` | Ver los mensajes de MariaDB (útil si algo falla) |
| `docker compose stop` | Pararlo sin borrar nada |
| `docker compose down` | Borrar el contenedor (los datos se conservan) |
| `docker compose down -v` | ⚠️ Borrar contenedor **y datos** |

Requisito: **Docker Desktop abierto**. Además, como no hemos configurado reinicio automático, tras reiniciar el ordenador hay que volver a ejecutar `docker compose up -d`.

## Resumen

- **Imagen** = plantilla; **contenedor** = ejecución desechable de esa plantilla.
- **Docker Compose** describe la infraestructura en `compose.yaml` y la arranca con un comando.
- Las **credenciales** viven en `.env` (no versionado); `.env.example` documenta qué hay que rellenar.
- **Puertos** `"3307:3306"`: 3307 en Windows, 3306 dentro del contenedor.
- Un **volumen** guarda los datos fuera del contenedor: sobreviven a `down`, no a `down -v`.
- **`utf8mb4`** permite guardar tildes; el **healthcheck** indica cuándo la base de datos está lista.

## Preguntas de repaso

**¿Qué diferencia hay entre una imagen y un contenedor?**
La imagen es la plantilla de solo lectura (`mariadb:12.3`); el contenedor es una ejecución de esa plantilla, con su propio estado, que se puede crear y borrar.

**¿Por qué la contraseña no está escrita en `compose.yaml`?**
Porque `compose.yaml` se sube al repositorio. La contraseña está en `.env`, que está en `.gitignore`.

**Si ejecuto `docker compose down`, ¿pierdo las tablas?**
No: el contenedor se borra, pero los datos están en el volumen `movies_mariadb-data`. Sí se perderían con `docker compose down -v`.

**¿A qué puerto debe conectarse nuestra aplicación Java y por qué?**
Al 3307 de `localhost`. El 3306 lo ocupa el MariaDB instalado en Windows.

**Si cambio `MARIADB_PASSWORD` en `.env` y reinicio, ¿cambia la contraseña?**
No. Esas variables solo se aplican al crear la base de datos con el volumen vacío.

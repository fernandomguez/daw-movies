# DAW Movies

Proyecto de 2.º de DAW: una aplicación de gestión de películas con **API REST de backend** y **SPA de frontend** en un monorepo ligero de dos carpetas.

- **Backend** — API REST de películas, directores, actores y valoraciones (1–10), construida con Java 25 + Spring Boot 4.1.1 como monolito modular (módulos `catalog` y `ratings`), base de datos MariaDB 12.3 en Docker y migraciones con Flyway. Expone 9 endpoints documentados con Swagger.
- **Frontend** — SPA en Angular 22 que consume esa API: listado de películas con tarjetas, formulario de alta/edición y modal de confirmación, con desplegables de directores y actores.

```
daw-movies/
├── backend/      ← API REST (Spring Boot)
├── frontend/     ← SPA (Angular)
└── README.md     ← este archivo
```

## backend/ (Spring Boot)

API REST en un monolito modular siguiendo la arquitectura del temario:

- **Java 25 + Spring Boot 4.1.1**, Maven vía Wrapper (`mvnw.cmd`).
- **MariaDB 12.3 en Docker** (`compose.yaml`, puerto `3307`, credenciales en `.env`) + **Flyway** para el esquema; SQL escrito a mano con `JdbcTemplate` (sin JPA).
- **Dos módulos sellados** con Spring Modulith + ArchUnit: `catalog` (películas, directores, actores) y `ratings` (valoraciones), cada uno con capas `api/http`, `internal/application`, `internal/domain` e `internal/persistence`.
- **Swagger UI** con springdoc (`/swagger-ui.html`) y **117 tests en verde** (TDD + reglas de arquitectura).

Arranque rápido y detalles completos: [`backend/README.md`](backend/README.md), que incluye la guía de estudio fase a fase de [`backend/docs/guia-estudio/`](backend/docs/guia-estudio/README.md).

```powershell
# En backend/ (Docker Desktop abierto)
copy .env.example .env        # y cambiar los "change_me"
docker compose up -d          # MariaDB
.\mvnw.cmd spring-boot:run    # API en http://localhost:8080
```

## frontend/ (Angular)

SPA con la nueva sintaxis de Angular 22 (componentes standalone, signals, control flow `@if/@for`) organizada en:

```
frontend/src/app/
├── core/          ← modelo Movie y MovieService (HttpClient)
├── features/      ← feature "movies": listado de películas
└── shared/        ← card-movies, form-movies y modal-movies reutilizables
```

- **Angular 22 + TypeScript + SCSS**, tests con Vitest (`ng test`) y Prettier.
- El `npm start` / `ng serve` usa `proxy.conf.json`: las rutas `/movies` se redirigen a `http://localhost:8080`, así el navegador no sufre CORS (mismo origen en desarrollo).

```powershell
# En frontend/ (con la API y MariaDB ya arrancadas)
npm install
npm start                # SPA en http://localhost:4200
```

## Flujo completo en desarrollo

1. Docker Desktop abierto → `docker compose up -d` (en `backend/`).
2. API arriba → `.\mvnw.cmd spring-boot:run` (en `backend/`).
3. SPA arriba → `npm start` (en `frontend/`).
4. Abrir `http://localhost:4200`.

> Más contexto del proyecto: normas en [`backend/base-standards.md`](backend/base-standards.md) y estructura y decisiones en la [guía de estudio del backend](backend/docs/guia-estudio/README.md).

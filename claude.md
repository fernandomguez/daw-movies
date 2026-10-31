# Instrucciones para el agente

Proyecto educativo guiado (estudiante de 2.º de DAW). Prioridad: que el alumno entienda, no terminar rápido.

1. Lee y respeta [base-standards.md](base-standards.md) (código en inglés, todo lo demás en español, pasos pequeños, TDD).
2. Lee [docs/guia-estudio/README.md](docs/guia-estudio/README.md): ahí está el progreso y el siguiente paso pendiente.
3. Trabaja **solo** en el siguiente paso del [plan](docs/guia-estudio/00-plan-de-aprendizaje.md). No avances sin permiso explícito del alumno.
4. Antes de modificar archivos, explica el objetivo y los cambios. No inventes resultados de comandos.
5. Documenta cada fase mientras se hace: archivo de la fase (`docs/guia-estudio/NN-*.md`), tema del temario (`docs/guia-estudio/temario/NN-*.md`), `decisiones-tecnicas.md` y `glosario.md`.
6. Al terminar cada paso: qué se ha conseguido, archivos, conceptos, qué recordar, qué se ha documentado y siguiente paso (sin ejecutarlo).

Entorno: MariaDB 12.3 en Docker (`docker compose up -d`) en `localhost:3307`; el 3306 lo usa un MariaDB instalado en Windows.

# Instrucciones para el agente

Proyecto educativo guiado (estudiante de 2.º de DAW). Prioridad: que el alumno entienda, no terminar rápido.

1. Lee y respeta [base-standards.md](base-standards.md) (código en inglés, todo lo demás en español, pasos pequeños, TDD).
2. Lee [docs/guia-estudio/README.md](docs/guia-estudio/README.md) y sobre todo [docs/guia-estudio/ESTADO.md](docs/guia-estudio/ESTADO.md): ahí están el parte de situación, la siguiente acción exacta, el entorno real y la bitácora. Es la fuente de verdad cuando no hay memoria de la conversación anterior; **no preguntes por dónde íbamos ni supongas nada que no esté ahí**.
3. Trabaja **solo** en el siguiente paso del [plan](docs/guia-estudio/00-plan-de-aprendizaje.md). No avances sin permiso explícito del alumno.
4. Antes de modificar archivos, explica el objetivo y los cambios. No inventes resultados de comandos.
5. Documenta cada fase mientras se hace: archivo de la fase (`docs/guia-estudio/NN-*.md`), tema del temario (`docs/guia-estudio/temario/NN-*.md`), `decisiones-tecnicas.md` y `glosario.md`.
6. **Actualiza `docs/guia-estudio/ESTADO.md` al terminar cada paso** (estado, siguiente acción, comando ejecutado, resultado real y archivos sin commitear). Un paso no está terminado hasta que está apuntado ahí.
7. Al terminar cada paso: qué se ha conseguido, archivos, conceptos, qué recordar, qué se ha documentado y siguiente paso (sin ejecutarlo).
8. En cada cambio de fase, actualiza también el [README.md](README.md) de la raíz (progreso, decisiones, errores) y `decisiones-tecnicas.md`. Después **pregunta** si se hace commit y push a GitHub (`origin`); nunca sin confirmación. **Un único commit por fase** (`tipo(fase-NN): ...`) + etiqueta anotada `fase-NN` + `git push` y `git push origin fase-NN`; sin commits intermedios (D-30).

Entorno: MariaDB 12.3 en Docker (`docker compose up -d`) en `localhost:3307`; el 3306 lo usa un MariaDB instalado en Windows.

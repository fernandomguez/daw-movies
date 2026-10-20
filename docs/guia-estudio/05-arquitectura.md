# 05 — Arquitectura en tres capas e inversión de dependencias

> **Conceptos:** [temario/05-arquitectura-tres-capas-dip.md](temario/05-arquitectura-tres-capas-dip.md).

## Objetivo

Decidir, antes de escribir código, qué responsabilidad tiene cada capa, cómo se relacionan y cómo se organizan en paquetes.

## Qué hicimos

| Paso | Resultado |
|---|---|
| 5.1 Las tres capas | Controlador (entrada/salida HTTP), dominio (modelo, reglas, casos de uso, puertos) y persistencia (entidades JPA, Spring Data, adaptadores, mappers). |
| 5.2 Dirección de las dependencias | Todas apuntan hacia el dominio. El dominio solo usa Java estándar. |
| 5.3 DIP | El dominio define `MovieRepository` (puerto); la persistencia lo implementa con `MovieRepositoryAdapter` (adaptador). |
| 5.4 Paquetes | Organización por capa: `controller`, `domain`, `persistence`, más `config` para el ensamblaje. Se crean a medida que se necesiten. |

## Comandos y comprobaciones

Ninguno: fase teórica. No se ha creado código.

## Decisiones

En [decisiones-tecnicas.md](decisiones-tecnicas.md): D-18 (paquetes por capa) y D-19 (dominio sin anotaciones de Spring, pendiente de confirmar).

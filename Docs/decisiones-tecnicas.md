# Decisiones Técnicas

Registro cronológico de decisiones no obvias y desvíos respecto al PRD original. Mantenido por el agente `documenter`.

---

### 2026-07-21 — Backend: Arquitectura Hexagonal (Ports & Adapters) en vez de capas clásicas (Controller/Service/Repository)

**Contexto:** El PRD original (RNF-05) especificaba "arquitectura en capas limpia (Controller, Service, Repository, DTO, Mapper)".

**Decisión:** Se reemplaza por Arquitectura Hexagonal. El núcleo de dominio (modelo + casos de uso + motor de reglas deterministas de progresión) queda sin dependencias de Spring/JPA. Los Controllers REST y el filtro JWT son adaptadores de entrada; las implementaciones de Spring Data JPA son adaptadores de salida, ambos implementando puertos (`in`/`out`) definidos por el dominio.

**Motivo:** El roadmap del PRD (§18 Ideas para Versiones Futuras) incluye integraciones externas que se acoplarían mal a una arquitectura en capas clásica: Health Connect/Google Fit, modo Entrenador/Cliente, import/export de rutinas por QR. Con hexagonal, cada una de estas se agrega como un adaptador nuevo sin modificar el núcleo de negocio ni el motor de reglas ya probado.

**Actualizado en:**
- `Docs/documentacion_funcional_tecnica_fitness_mvp.md` — RNF-05 y diagrama de arquitectura §8.
- `.claude/agents/backend-java-senior.md` — principios de arquitectura del agente.

**Nota:** El módulo Android (MVVM + Clean Architecture ligera) no cambia — esta decisión es exclusiva del backend.

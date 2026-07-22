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

---

### 2026-07-21 — Backend: Gradle (Kotlin DSL) en vez de Maven

**Contexto:** El scaffold inicial del backend se armó con Maven (`pom.xml`). El usuario pidió cambiarlo a Gradle antes de seguir avanzando.

**Decisión:** Se reemplaza Maven por Gradle 8.10 con Kotlin DSL (`build.gradle.kts`), wrapper commiteado (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) para no depender de una instalación local de Gradle.

**Motivo:** Consistencia de tooling con el lado Android del proyecto (Gradle es obligatorio ahí) y mejor compatibilidad/tooling con Kotlin a futuro si se comparte código o convenciones entre backend y mobile.

**Verificación:** se migraron las mismas dependencias 1:1 (Spring Boot BOM vía plugin `io.spring.dependency-management`), se corrieron los 5 tests unitarios (pasan igual que con Maven) y se repitió la prueba manual end-to-end (Postgres real vía docker-compose + `./gradlew bootRun` + registro/login) con resultado idéntico.

**Detalle no obvio:** el patrón `!gradle/wrapper/gradle-wrapper.jar` en `.gitignore` no alcanza subcarpetas (solo desbloquea esa ruta en la raíz del repo); se corrigió a `!**/gradle/wrapper/gradle-wrapper.jar` para que el jar del wrapper de `backend/` quede versionado y no se pierda al clonar.

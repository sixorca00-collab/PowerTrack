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

---

### 2026-07-22 — Backend: `RoutineExercise` (no `Exercise`) como ancla del registro de progreso

**Contexto:** el módulo de Registro/Progreso necesita comparar el desempeño de cada serie contra un rango de repeticiones objetivo para poder aplicar la matriz de decisión del PRD §14.

**Decisión:** `WorkoutLog` (y toda la cadena: `previous-log`, `finish`, `ProgressionRuleEngine`) referencia siempre `routineExerciseId`, nunca `exerciseId` directamente. `RoutineExercise` es la personalización del usuario dentro de una rutina concreta (sus propios `targetRepMin`/`targetRepMax`/`targetSets`); `Exercise` es solo el catálogo (nombre, músculo, categoría), sin targets.

**Motivo:** dos usuarios (o el mismo usuario en dos rutinas distintas) pueden entrenar el mismo `Exercise` con rangos de reps objetivo diferentes. Anclar el registro a `exerciseId` obligaría a buscar el target "correcto" con una regla implícita ambigua; anclarlo a `routineExerciseId` hace que el target sea siempre no ambiguo y explícito (el que el propio usuario configuró en esa rutina).

---

### 2026-07-22 — Backend: ownership de recursos ajenos devuelve 404, no 403 (Rutinas y Registro/Progreso)

**Contexto:** al consultar/eliminar una rutina, iniciar una sesión sobre un `routineDayId`, o loguear contra un `routineExerciseId`, el recurso puede existir pero pertenecer a otro usuario.

**Decisión:** en todos estos casos se lanza una excepción de "no encontrado" (`RoutineNotFoundException`, `RoutineDayNotFoundException`, `RoutineExerciseNotFoundException`, `WorkoutSessionNotFoundException`) mapeada a `404`, nunca a `403`. El filtro de dueño se aplica directamente en la query (`findByIdAndUserId`, `deleteByIdAndUserId`, `existsRoutineDayOwnedByUser`, `findRoutineExerciseTargets(id, userId)`), no como un chequeo posterior con distinción de error.

**Motivo:** mismo criterio anti-enumeración ya establecido por `InvalidCredentialsException` en Auth (ver entrada previa de este documento) — un `403` le confirmaría a un atacante que el recurso existe pero no es suyo; un `404` no distingue "no existe" de "no es tuyo".

---

### 2026-07-22 — Backend: orden de evaluación 5→4→3→2→1 en `ProgressionRuleEngine` y gap del PRD §14 resuelto como `MAINTAIN` por defecto

**Contexto:** el PRD §14 define 5 reglas de la "Matriz de Decisión de Progresión" pero no explicita en qué orden evaluarlas cuando más de una condición se cumple simultáneamente, ni qué hacer cuando ninguna regla aplica literalmente.

**Decisión:** se evalúa en orden 5 → 4 → 3 → 2 → 1 (de mayor a menor severidad: Deload > Reducir Peso > Mantener > Aumentar Reps > Aumentar Peso). Cuando ninguna de las 5 reglas aplica (gap detectado: minoría de series bajo `target_min` sin que se cumpla ninguna otra condición), el motor devuelve `Recommendation.MAINTAIN` como valor por defecto.

**Motivo:** las señales de fatiga/riesgo deben ganar siempre sobre las señales de progreso — progresar sobre una base de fatiga no detectada es el peor resultado posible del motor. El `MAINTAIN` por defecto para el gap es la opción más segura disponible sin inventar una regla 6 no solicitada. Esta decisión fue confirmada explícitamente por el usuario (no queda como pendiente ni bloqueante); si producto decide más adelante que el gap merece una regla propia, es un cambio de alcance, no un bug. Detalle completo de supuestos (RPE máximo/mínimo por ejercicio, ventana histórica de 2 sesiones + la actual) documentado en el Javadoc de `ProgressionRuleEngine`.

---

### 2026-07-22 — Backend: `Set` (no `List`) en `RoutineJpaEntity.days` para evitar `MultipleBagFetchException`

**Contexto:** al mapear el agregado `Routine -> RoutineDay -> RoutineExercise` con Hibernate, ambos niveles de colección (`days` y `exercises`) empezaron como `List` sin `@OrderColumn` ("bag" semantics).

**Decisión:** `RoutineJpaEntity.days` se tipó como `Set` (`LinkedHashSet`) en vez de `List`; `RoutineDayJpaEntity.exercises` se mantuvo como `List`. El orden real se sigue garantizando con `@OrderBy("orderIndex ASC")` en JPA y, además, se reordena defensivamente en `RoutinePersistenceAdapter` al mapear hacia el dominio.

**Motivo:** Hibernate no permite hacer fetch join de dos colecciones "bag" (List sin índice) simultáneamente en la misma consulta (`MultipleBagFetchException`) cuando se cargan `days.exercises` anidados. Convertir un solo nivel a `Set` resuelve la ambigüedad sin perder el orden, que de todas formas no depende de la semántica de la colección Java sino del `@OrderBy`/reordenado explícito.

---

### 2026-07-22 — Inconsistencia detectada en el PRD (no corregida): RF-06 lista 6 recomendaciones, §14.1 define solo 5

**Contexto (reportado, no una decisión propia):** RF-06 del PRD enumera 6 salidas posibles del motor de sugerencias: "Aumentar peso, Mantener peso, Reducir peso, Aumentar repeticiones, Recomendar descarga/Deload, **Recomendar descanso adicional**". La Matriz de Decisión de §14.1, que es la especificación operacional real (condiciones exactas por regla), solo define 5 reglas — no incluye ninguna condición para "descanso adicional".

**Estado de la implementación:** `Recommendation` (enum de dominio) y `ProgressionRuleEngine` implementan exactamente las 5 reglas de §14.1 (`INCREASE_WEIGHT`, `INCREASE_REPS`, `MAINTAIN`, `DECREASE_WEIGHT`, `DELOAD`). "Recomendar descanso adicional" de RF-06 no tiene condición definida en ningún lado del PRD y no fue implementado.

**No se edita el PRD por esta vía** (fuera del alcance de este agente); se señala aquí para que producto decida si RF-06 debe recortarse a 5 salidas (alinearlo con §14.1) o si falta definir una 6ª regla con su condición exacta.

---

### 2026-07-23 — Backend: Regla 2 (`ProgressionRuleEngine`) extendida de RPE {7,8} a RPE ≤8

**Contexto:** revisión pre-testers detectó un segundo gap no documentado, distinto del ya conocido (minoría de series bajo `target_min`): con reps ya dentro de `[target_min, target_max)` pero RPE bajo (1-6), ninguna de las 5 reglas del PRD §14.1 aplicaba (Regla 1 exige llegar a `target_max`; Regla 2 exigía RPE exactamente en {7,8}; Regla 3 exige RPE=9 o sensación Regular). El motor devolvía `MAINTAIN` por el mismo fallback por defecto, pero a diferencia del gap original este caso no es raro: cualquier serie con esfuerzo cómodo (RPE 1-6) y reps intermedias lo dispara.

**Decisión:** la condición de RPE de la Regla 2 pasa de `RPE == 7 || RPE == 8` a `RPE <= 8` (mismo techo que ya usa la Regla 1), sin tocar las Reglas 1, 3, 4 o 5. Verificado que no genera colisiones: la Regla 3 (Mantener) se evalúa antes que la Regla 2 en el orden 5→4→3→2→1, así que los casos con sensación Regular o RPE=9 se siguen resolviendo ahí sin llegar nunca a la Regla 2 ampliada.

**Motivo:** el objetivo del producto es sobrecarga progresiva. Que un usuario con esfuerzo bajo (RPE 1-6) y reps ya dentro de rango reciba "Mantener" por defecto, en vez de "Aumentar Repeticiones", contradice ese objetivo — tiene margen real para progresar. El gap original (minoría bajo `target_min`) sigue sin regla propia y sigue siendo una decisión de producto pendiente, no afectada por este cambio.

**Verificación:** `ProgressionRuleEngineTest` actualizado (el caso RPE=6 que antes afirmaba "no califica" ahora afirma lo contrario, más un caso límite en RPE=1); suite completa del backend sigue en verde.

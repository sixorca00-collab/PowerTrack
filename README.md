# PowerTrack

App de registro de entrenamiento de fuerza y cardio, offline-first, con backend en Spring Boot (arquitectura hexagonal) y cliente Android nativo (Kotlin + Jetpack Compose).

Ver `Docs/` para la especificación funcional/técnica completa y el alcance priorizado del MVP.

## Flujo de ramas (GitFlow)

- `main` — solo releases estables. No se commitea directo acá.
- `develop` — rama de integración, donde vive el trabajo activo.
- `feature/*` — una por funcionalidad, sale de `develop` y vuelve a `develop`.
- `release/*` — estabilización antes de mergear a `main`.
- `hotfix/*` — fixes urgentes sobre `main`.

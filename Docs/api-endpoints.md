# API Endpoints

Tabla viva de endpoints reales del backend. Ver diseño completo propuesto en PRD §12; acá solo lo que ya existe en código.

| Método | Ruta | Propósito | Auth | Estado | Módulo |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/register` | Registrar usuario nuevo con objetivo deportivo (RF-01) | No | Implementado | Auth |
| POST | `/api/v1/auth/login` | Autenticar y devolver `accessToken`/`refreshToken` | No | Implementado | Auth |
| GET | `/api/v1/users/me` | Obtener el perfil del usuario autenticado | Sí | Implementado | Perfil de Usuario |
| PUT | `/api/v1/users/me/goal` | Actualizar el `sportGoal` del usuario autenticado | Sí | Implementado | Perfil de Usuario |
| POST | `/api/v1/routines` | Crear una rutina completa (nombre + días + ejercicios con targets) | Sí | Implementado | Rutinas |
| GET | `/api/v1/routines` | Listar rutinas (resumen) del usuario autenticado | Sí | Implementado | Rutinas |
| GET | `/api/v1/routines/{id}` | Obtener el detalle completo de una rutina propia (días, ejercicios, targets) | Sí | Implementado | Rutinas |
| DELETE | `/api/v1/routines/{id}` | Eliminar una rutina propia (cascada a días/ejercicios) | Sí | Implementado | Rutinas |
| GET | `/api/v1/exercises` | Listar catálogo de ejercicios: predefinidos + personalizados del usuario | Sí | Implementado | Rutinas |
| POST | `/api/v1/exercises` | Crear un ejercicio personalizado (`createdByUserId` = usuario autenticado) | Sí | Implementado | Rutinas |
| POST | `/api/v1/workouts/session/start` | Iniciar una sesión de entrenamiento (`IN_PROGRESS`) para un `routineDayId` propio | Sí | Implementado | Registro/Progreso |
| GET | `/api/v1/workouts/previous-log` | Obtener el último registro (sets) de un `routineExerciseId`, para pre-llenar el Live Tracker | Sí | Implementado | Registro/Progreso |
| POST | `/api/v1/workouts/session/{id}/finish` | Finalizar sesión: persiste logs/sets y devuelve sugerencias de progresión por ejercicio | Sí | Implementado | Registro/Progreso |

**Request/response (resumido):**
- `register`/`login` devuelven `{ userId, email, accessToken, refreshToken }`.
- `register` body: `{ email, password (min 8), fullName, sportGoal }`. `sportGoal` es uno de los 10 valores de RF-01 (ej. `POWERLIFTING`, `HIPERTROFIA`).
- Errores: `400` validación, `401` credenciales inválidas (mensaje genérico, no distingue email inexistente de password incorrecta), `409` email ya registrado.
- Todo lo que no sea `/api/v1/auth/**` requiere header `Authorization: Bearer <accessToken>` (Spring Security lo exige por defecto, devuelve `403` si falta).
- `GET /users/me` y `PUT /users/me/goal` devuelven/reciben `{ id, email, fullName, sportGoal, createdAt }` (body de `PUT`: `{ sportGoal }`). `404` si el usuario del token no existe (caso borde, no debería ocurrir en la práctica).
- `POST /routines` body: `{ name, description?, days: [{ dayName, orderIndex, exercises: [{ exerciseId, orderIndex, targetSets, targetRepMin, targetRepMax, restSeconds?, notes? }] }] }`. Responde `201` con el detalle completo (incluye `exerciseName` resuelto contra el catálogo). `404` si algún `exerciseId` referenciado no existe en el catálogo.
- `GET /routines` devuelve resumen (`id, name, description, createdAt`, sin días/ejercicios); `GET /routines/{id}` devuelve el detalle anidado completo.
- `GET /routines/{id}` y `DELETE /routines/{id}`: `404` tanto si la rutina no existe como si pertenece a otro usuario (ver `Docs/decisiones-tecnicas.md`, no se usa `403`).
- `GET /exercises` devuelve `[{ id, name, targetMuscle, category, predefined }]` — incluye los 10 ejercicios predefinidos (seed `V3__seed_exercises.sql`) más los personalizados del usuario autenticado. `POST /exercises` body: `{ name, targetMuscle, category }`, responde `201`.
- `POST /workouts/session/start` body: `{ routineDayId }`, responde `201` con `{ sessionId, routineDayId, startTime }`. `404` si el `routineDayId` no existe o no pertenece (indirectamente, vía la rutina) al usuario.
- `GET /workouts/previous-log?routineExerciseId=...` responde `200` con `{ workoutLogId, routineExerciseId, performedAt, notes, sets: [{ setNumber, weightKg, repsCompleted, rpe }] }` del último log completado, o `204 No Content` (sin body) si no hay histórico — decisión explícita para no forzar al cliente a parsear un body nulo.
- `POST /workouts/session/{id}/finish` body: `{ overallFeeling, exerciseLogs: [{ routineExerciseId, notes?, sets: [{ setNumber, weightKg, repsCompleted, rpe (1-10) }] }] }`. Responde `200` con `{ sessionId, startTime, endTime, overallFeeling, suggestions: [{ routineExerciseId, recommendation, message }] }` — `recommendation` es uno de `INCREASE_WEIGHT | INCREASE_REPS | MAINTAIN | DECREASE_WEIGHT | DELOAD` (ver `ProgressionRuleEngine` en `Docs/modulos-y-flujos.md`). `404` si la sesión o algún `routineExerciseId` no pertenecen al usuario; `409` si la sesión ya estaba `COMPLETED`.

**Pendientes** (según PRD §12.5-§12.6, no implementados aún):
- Cardio Log: `POST /api/v1/cardio`, `GET /api/v1/cardio`.
- Analítica y Progreso: `GET /api/v1/analytics/exercise/{exerciseId}`, `GET /api/v1/analytics/muscle-group`.

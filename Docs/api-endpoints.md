# API Endpoints

Tabla viva de endpoints reales del backend. Ver diseño completo propuesto en PRD §12; acá solo lo que ya existe en código.

| Método | Ruta | Propósito | Auth | Estado | Módulo |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/register` | Registrar usuario nuevo con objetivo deportivo (RF-01) | No | Implementado | Auth |
| POST | `/api/v1/auth/login` | Autenticar y devolver `accessToken`/`refreshToken` | No | Implementado | Auth |

**Request/response (resumido):**
- `register`/`login` devuelven `{ userId, email, accessToken, refreshToken }`.
- `register` body: `{ email, password (min 8), fullName, sportGoal }`. `sportGoal` es uno de los 10 valores de RF-01 (ej. `POWERLIFTING`, `HIPERTROFIA`).
- Errores: `400` validación, `401` credenciales inválidas (mensaje genérico, no distingue email inexistente de password incorrecta), `409` email ya registrado.
- Todo lo que no sea `/api/v1/auth/**` requiere header `Authorization: Bearer <accessToken>` (Spring Security lo exige por defecto, devuelve `403` si falta).

**Pendientes** (según PRD §12, no implementados aún): `GET /api/v1/users/me`, `PUT /api/v1/users/me/goal`, todo el módulo de Rutinas, Ejercicios, Sesiones/Workouts, Cardio y Analítica.

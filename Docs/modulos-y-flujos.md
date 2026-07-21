# Módulos y Flujos

## Auth (backend)

**Objetivo:** dar de alta usuarios y emitir/validar JWT para que el resto de los módulos puedan asociar datos a un `userId` autenticado.

**Compone:** `POST /api/v1/auth/register`, `POST /api/v1/auth/login` (ver `Docs/api-endpoints.md`).

**Flujo de datos (registro):**
```
AuthController (adaptador in)
  -> RegisterUserUseCase (puerto in)
  -> RegisterUserService (application)
       -> UserRepositoryPort.existsByEmail  -> si existe: EmailAlreadyRegisteredException (409)
       -> PasswordHasherPort.hash           (BCrypt, adaptador infra)
       -> User.register(...)                (dominio puro, sin JPA/Spring)
       -> UserRepositoryPort.save           (adaptador JPA -> tabla `users`, migración V1)
       -> TokenProviderPort.generate*Token  (JWT HS256, adaptador infra)
  <- AuthResult { userId, email, accessToken, refreshToken }
```

**Flujo de datos (login):** igual pero vía `AuthenticateUserUseCase` — busca por email, compara hash con `PasswordHasherPort.matches`, mismo `InvalidCredentialsException` genérico tanto si el email no existe como si la password es incorrecta (evita enumeración de usuarios).

**Requests protegidos:** `JwtAuthenticationFilter` (adaptador in, infraestructura pura — no pasa por ningún puerto de aplicación) valida el `Bearer` token en cada request no-auth y puebla el `SecurityContext` con el `userId` como principal.

**Estado:** implementado y verificado end-to-end (compilación, tests unitarios de `RegisterUserService`/`AuthenticateUserService`, y prueba manual contra Postgres real vía docker-compose: registro, duplicado, login OK, login con password incorrecta, validación de input, endpoint protegido sin token).

**Próximo módulo a construir:** Rutinas (creación/estructura de días y ejercicios) — ver prioridad en `Docs/propuesta-modulos-rutinas-y-registro.md`. Va a reusar el mismo patrón hexagonal: dominio en `domain/routine`, puertos en `application/routine`, adaptadores en `infrastructure/adapter/{in/web, out/persistence}`.

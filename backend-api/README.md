# buildzone-api

API REST de **BuildZone**, construida con Spring Boot. Es el backend que consume el
front-end de la raíz del proyecto (`index.html` + `api.js`) y reúne tres módulos:

| Módulo | Qué hace | Endpoints |
|---|---|---|
| **Catálogo** | CRUD de Producto, Marca y Categoría sobre las tablas que ya existen en la base `buildzone` (las mismas que usa `backend-java`) | `/api/productos`, `/api/marcas`, `/api/categorias` |
| **Seguridad y usuarios** | Registro, inicio de sesión con JWT, perfil propio, cambio de contraseña y administración de usuarios por rol | `/api/auth`, `/api/usuarios` |
| **Planes y suscripciones** | Planes de pago y suscripción de cada usuario (alta, cancelación, vencimiento, historial) | `/api/planes`, `/api/suscripciones` |

`backend-java` (consola JDBC) sigue existiendo y no se modificó: las dos aplicaciones
trabajan sobre la misma base de datos.

## Tecnologías por capa

| Capa | Tecnología / librería |
|---|---|
| Lenguaje y build | Java 21, Maven, Spring Boot 3.3.4 |
| Presentación (API REST) | Spring Web (`@RestController`), Jackson (JSON), Bean Validation |
| Seguridad (transversal) | Spring Security 6, JJWT 0.12.6 (tokens JWT HS256), BCrypt |
| Lógica de negocio | Servicios Spring (`@Service`) con transacciones (`@Transactional`) |
| Acceso a datos | Spring Data JPA, Hibernate 6 |
| Base de datos | MySQL/MariaDB (XAMPP) en el perfil por defecto; H2 en memoria en `dev` y en las pruebas |
| Pruebas | JUnit 5, Mockito, AssertJ, MockMvc, spring-security-test |
| Control de versiones | Git |

## Arquitectura y paquetes

La API sigue una arquitectura **en capas**. Cada petición recorre
`controller → service → repository → base de datos`, y ninguna capa se salta a la siguiente.

```
src/main/java/com/buildzone/api/
├── BuildzoneApiApplication.java  Punto de entrada
├── controller/     Capa de presentación: recibe HTTP, valida DTOs y delega
│   ├── AuthController, UsuarioController
│   ├── PlanController, SuscripcionController
│   └── ProductoController, MarcaController, CategoriaController
├── service/        Capa de negocio: interfaces (contratos de casos de uso)
│   └── impl/       Implementaciones con las reglas de negocio
├── repository/     Capa de datos: interfaces Spring Data JPA
├── model/          Entidades JPA (Usuario, PlanSuscripcion, Suscripcion, Producto, Marca, Categoria)
│   └── enums/      Rol, EstadoUsuario, EstadoSuscripcion
├── dto/            Objetos de entrada/salida (la entidad nunca sale al cliente)
├── security/       JwtService, JwtAuthenticationFilter, UsuarioAutenticado, RespuestaErrorSeguridad
├── config/         SecurityConfig, AppConfig (Clock), DatosInicialesConfig, CatalogoDemoConfig
└── exception/      GlobalExceptionHandler, ErrorResponse y excepciones de negocio
```

### Patrones de diseño aplicados

| Patrón | Dónde |
|---|---|
| **MVC / arquitectura en capas** | `controller` → `service` → `repository` |
| **Repository** | Interfaces `*Repository` de Spring Data: la persistencia queda aislada del negocio |
| **DTO** | `*Request` / `*Response`: validan la entrada y ocultan datos sensibles (el hash de la contraseña nunca sale) |
| **Inyección de dependencias** | Todas las dependencias entran por constructor y se programa contra interfaces (`AuthService`, no `AuthServiceImpl`) |
| **Chain of Responsibility** | `JwtAuthenticationFilter` dentro de la cadena de filtros de Spring Security |
| **Static Factory Method** | `UsuarioResponse.desde(...)`, `PlanResponse.desde(...)`, `ErrorResponse.de(...)` |
| **Controller Advice** (manejo centralizado de errores) | `GlobalExceptionHandler` convierte cada excepción en un `ErrorResponse` uniforme |
| **Strategy (reloj inyectable)** | `Clock` se inyecta en `SuscripcionServiceImpl` para fijar la fecha en las pruebas |

## Mecanismos de seguridad

- **Autenticación sin estado con JWT**: `POST /api/auth/login` devuelve un token firmado
  con HMAC-SHA256 que vence en 8 horas. El cliente lo envía en `Authorization: Bearer <token>`.
- **Contraseñas con BCrypt**: nunca se guardan ni se devuelven en texto plano.
- **Autorización por rol** (`USUARIO` / `ADMIN`) en `SecurityConfig`, por ruta y verbo HTTP.
- **Revalidación en cada petición**: el filtro vuelve a leer el usuario en la base, así que
  desactivar una cuenta o quitarle el rol ADMIN tiene efecto inmediato aunque su token siga vigente.
- **Reglas de negocio de seguridad**: un ADMIN no puede quitarse su propio rol ni desactivarse;
  un usuario solo puede cancelar sus propias suscripciones; el perfil `/me` siempre usa el id del
  token, nunca uno enviado por el cliente.
- **Mensajes de login que no filtran información**: el mismo error para "usuario no existe" y
  "contraseña incorrecta".
- **Validación de entrada** con Bean Validation en todos los DTO.
- **Errores sin trazas internas**: `GlobalExceptionHandler` y `server.error.include-stacktrace=never`.
- **CORS** configurable (`buildzone.cors.origenes-permitidos`) y CSRF deshabilitado (no hay cookies de sesión).
- **Secretos por variables de entorno**: `JWT_SECRET`, `DB_USER`, `DB_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
- En el front-end, todo dato del usuario se escapa con `escaparHtml` antes de insertarlo en el HTML (anti-XSS).

### Matriz de permisos

| Ruta | Público | USUARIO | ADMIN |
|---|:-:|:-:|:-:|
| `POST /api/auth/registro`, `POST /api/auth/login` | ✅ | ✅ | ✅ |
| `GET /api/productos`, `/api/marcas`, `/api/categorias` (y `/{id}`) | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE` de productos, marcas y categorías | ❌ 401 | ❌ 403 | ✅ |
| `GET /api/planes` | ✅ | ✅ | ✅ |
| `GET /api/planes/todos`, `POST/PUT/DELETE /api/planes` | ❌ 401 | ❌ 403 | ✅ |
| `GET/PUT /api/usuarios/me`, `PUT /api/usuarios/me/password` | ❌ 401 | ✅ | ✅ |
| `GET /api/usuarios`, `PATCH /api/usuarios/{id}/rol`, `PATCH /api/usuarios/{id}/estado` | ❌ 401 | ❌ 403 | ✅ |
| `POST /api/suscripciones`, `GET /api/suscripciones/me`, `GET /api/suscripciones/me/activa` | ❌ 401 | ✅ | ✅ |
| `DELETE /api/suscripciones/{id}` | ❌ 401 | ✅ (solo las propias) | ✅ |
| `GET /api/suscripciones` | ❌ 401 | ❌ 403 | ✅ |

## Endpoints

Base URL local: `http://localhost:8080`

### Autenticación — `/api/auth`

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/api/auth/registro` | `{"nombre","apellido","username","email","password"}` | 201 `{token, tipo:"Bearer", usuario}` · 400 · 409 |
| POST | `/api/auth/login` | `{"identificador": "correo o username", "password"}` | 200 `{token, tipo, usuario}` · 401 · 403 (cuenta inactiva) |

### Usuarios — `/api/usuarios`

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/usuarios/me` | — | 200 usuario |
| PUT | `/api/usuarios/me` | `{"nombre","apellido","email"}` | 200 usuario · 409 correo en uso |
| PUT | `/api/usuarios/me/password` | `{"passwordActual","passwordNueva"}` | 204 · 400 |
| GET | `/api/usuarios` | — | 200 lista (ADMIN) |
| PATCH | `/api/usuarios/{id}/rol` | `{"rol":"ADMIN"}` | 200 (ADMIN) |
| PATCH | `/api/usuarios/{id}/estado` | `{"estado":"INACTIVO"}` | 200 (ADMIN) |

Usuario de respuesta:
```json
{ "id": 1, "nombre": "Ana", "apellido": "Gómez", "username": "ana.gomez",
  "email": "ana@buildzone.com", "rol": "USUARIO", "estado": "ACTIVO",
  "fechaRegistro": "2026-09-23T10:15:00" }
```

### Planes — `/api/planes`

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/planes` | — | 200 planes activos, del más barato al más caro |
| GET | `/api/planes/todos` | — | 200 todos (ADMIN) |
| POST | `/api/planes` | `{"nombre","descripcion","precio","duracionDias"}` | 201 (ADMIN) · 409 |
| PUT | `/api/planes/{id}` | igual que POST | 200 (ADMIN) |
| DELETE | `/api/planes/{id}` | — | 204: borrado lógico, `activo=false` (ADMIN) |

### Suscripciones — `/api/suscripciones`

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/api/suscripciones` | `{"planId": 2}` | 201. Si ya había una suscripción activa a otro plan, esa pasa a `CANCELADA` |
| GET | `/api/suscripciones/me/activa` | — | 200 la vigente · 204 si no hay |
| GET | `/api/suscripciones/me` | — | 200 historial propio |
| DELETE | `/api/suscripciones/{id}` | — | 204 · 403 si es de otro usuario · 400 si no está activa |
| GET | `/api/suscripciones` | — | 200 todas (ADMIN) |

Suscripción de respuesta:
```json
{ "id": 5, "usuarioId": 1,
  "plan": { "id": 2, "nombre": "Premium", "descripcion": "...", "precio": 29900.00, "duracionDias": 30, "activo": true },
  "fechaInicio": "2026-09-23", "fechaFin": "2026-10-23", "estado": "ACTIVA" }
```

### Catálogo — `/api/productos`, `/api/marcas`, `/api/categorias`

Cada recurso tiene los 5 endpoints CRUD (`GET` lista, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`).

- Marca / Categoría: body `{"nombre","descripcion"}`, nombre único (409 si se repite). Si se
  intenta borrar una marca o categoría que todavía tiene productos, la respuesta es 409.
- Producto: body `{"nombre","descripcion","imagen","idMarca","idCategoria"}`. La respuesta
  incluye `nombreMarca` y `nombreCategoria`.

### Formato de error (todas las rutas)

```json
{ "timestamp": "2026-09-23T10:15:00", "status": 400, "error": "Bad Request",
  "mensaje": "Los datos enviados no son validos.", "ruta": "/api/auth/registro",
  "detalles": ["El correo no tiene un formato valido."] }
```

| Código | Cuándo |
|---|---|
| 400 | Validación, JSON mal formado o regla de negocio (contraseña actual incorrecta, plan inactivo...) |
| 401 | Sin token, token vencido o alterado, o credenciales incorrectas |
| 403 | Rol insuficiente, cuenta inactiva o recurso de otro usuario |
| 404 | Id o ruta inexistente |
| 409 | Nombre, correo o username repetido; integridad referencial |
| 500 | Error inesperado (se registra en el log, sin detalles internos para el cliente) |

## Ambientes

| Ambiente | Perfil | Base de datos | Uso |
|---|---|---|---|
| Desarrollo rápido | `dev` | H2 en memoria, tablas creadas por Hibernate, catálogo de ejemplo | Probar todo sin XAMPP |
| Pruebas automatizadas | `test` | H2 en memoria (`src/test/resources/application-test.properties`) | `mvn test` |
| Desarrollo con XAMPP | (por defecto) | MySQL/MariaDB `buildzone` en `localhost:3306` | Datos reales |
| Producción (Render) | `prod` | H2 en memoria (o MySQL externo con `DATABASE_URL`) | Demo pública desplegada con Docker |

### Opción A: sin XAMPP (perfil dev)

```bash
cd backend-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- API: `http://localhost:8080`
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:buildzone`, usuario `sa`, sin contraseña)
- Administrador inicial: `admin@buildzone.com` / `Admin1234`

### Opción B: con MySQL/MariaDB (XAMPP)

1. Inicie Apache y MySQL en XAMPP.
2. Ejecute **una sola vez** `database/mysql/01_usuarios_suscripciones.sql` sobre la base `buildzone`
   (phpMyAdmin → buildzone → pestaña SQL → pegar y ejecutar). Crea las tablas `usuario_cuenta`,
   `plan_suscripcion` y `suscripcion` **sin tocar** las 10 tablas que ya existen.
3. (Opcional) `database/mysql/02_datos_catalogo_ejemplo.sql` si el catálogo está vacío.
4. Arranque la API:
   ```bash
   cd backend-api
   mvn spring-boot:run
   ```
   Al primer arranque se crean automáticamente el usuario ADMIN y los planes Gratuito, Premium
   y Premium Anual.

En NetBeans o IntelliJ basta con abrir la carpeta `backend-api` (se reconoce el `pom.xml`) y
ejecutar `BuildzoneApiApplication`. Para el perfil dev agregue `-Dspring.profiles.active=dev`
en las opciones de la VM, o `spring.profiles.active=dev` en *Run → Set Project Configuration*.

### Variables de configuración

| Propiedad | Variable de entorno | Valor por defecto |
|---|---|---|
| `server.port` | `PORT` | `8080` |
| `spring.datasource.username` | `DB_USER` | `root` |
| `spring.datasource.password` | `DB_PASSWORD` | *(vacío, como XAMPP)* |
| `buildzone.jwt.secret` | `JWT_SECRET` | clave de ejemplo: **cámbiela en producción** (Base64, mínimo 256 bits) |
| `buildzone.jwt.expiracion-ms` | `BUILDZONE_JWT_EXPIRACION_MS` | `28800000` (8 h) |
| `buildzone.admin.email` | `ADMIN_EMAIL` | `admin@buildzone.com` |
| `buildzone.admin.password` | `ADMIN_PASSWORD` | `Admin1234` |
| `buildzone.cors.origenes-permitidos` | `BUILDZONE_CORS_ORIGENES_PERMITIDOS` | `*` |

Para generar una clave JWT propia: `openssl rand -base64 48`.

## Pruebas

```bash
cd backend-api
mvn test
```

| Clase de prueba | Tipo | Qué verifica |
|---|---|---|
| `JwtServiceTest` | Unitaria | Token válido, vencido, firmado con otra clave, texto basura, secreto no Base64 |
| `AuthServiceImplTest` | Unitaria (Mockito) | Registro, duplicados, login por correo/username, credenciales malas, cuenta inactiva |
| `UsuarioServiceImplTest` | Unitaria (Mockito) | Perfil, correo duplicado, cambio de contraseña, un ADMIN no puede quitarse su rol ni desactivarse |
| `PlanServiceImplTest` | Unitaria (Mockito) | Listado, creación, duplicado, borrado lógico, 404 |
| `SuscripcionServiceImplTest` | Unitaria (Mockito + reloj fijo) | Cálculo de fechas, cancelación de la anterior, plan inactivo, vencimiento, quién puede cancelar |
| `MarcaServiceImplTest`, `ProductoServiceImplTest` | Unitaria (Mockito) | Reglas del catálogo (nombre único, marca/categoría existentes) |
| `ApiIntegracionTest` | Integración (MockMvc + H2) | Contrato HTTP completo y matriz de permisos 401/403 |

La interfaz web se valida aparte con Playwright (`../validacion/e2e_buildzone.mjs`, 47 verificaciones).

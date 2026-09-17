# Informe de pruebas — BuildZone + backend real

Este documento resume las pruebas de integración frontend/backend realizadas
sobre este proyecto (BuildZone), siguiendo la misma metodología usada
anteriormente con "PC Compare" (React): una suite automatizada contra un
backend simulado que respeta el contrato real, más una verificación directa
contra el backend Spring Boot real corriendo en el PC.

## 1. Suite automatizada (Playwright + backend simulado)

Archivo: `validacion/e2e_buildzone.mjs`. Sirve el sitio de forma estática y
sustituye únicamente las llamadas HTTP a `http://localhost:8080/api/**` por
un mock que reproduce con exactitud las rutas, verbos, DTOs y códigos de
estado de los controladores reales (`AuthController`, `UsuarioController`,
`PlanSuscripcionController`, `SuscripcionController`).

**Resultado: 33/33 verificaciones correctas**, sin ninguna excepción de
JavaScript durante todo el recorrido. Cubre: navegación entre páginas,
buscador y comparador de productos, modal de artículos, formulario de
contacto, registro y login (casos válidos e inválidos), planes y
suscripción (alta y cancelación), Mi cuenta (perfil, contraseña,
historial) y el panel de administración completo (usuarios, planes,
suscripciones).

Cómo volver a ejecutarla (requiere Node.js y Playwright con Chromium
instalado):

```bash
cd validacion
node e2e_buildzone.mjs
```

## 2. Verificación contra el backend real

Con el backend Spring Boot real corriendo (`iniciar-backend.bat`, perfil
`dev` con H2 en memoria), se probaron directamente sus endpoints reales
(no simulados) para confirmar que `api.js` habla el mismo idioma que el
backend de verdad:

| Prueba | Endpoint real | Resultado |
|---|---|---|
| Registro de usuario nuevo | `POST /api/auth/registro` | **201**, token JWT y usuario devueltos correctamente |
| Consultar mi perfil | `GET /api/usuarios/me` | **200**, datos correctos |
| Actualizar mi perfil | `PUT /api/usuarios/me` | **200**, cambios reflejados |
| Cambiar mi contraseña | `PUT /api/usuarios/me/password` | **204** |
| Iniciar sesión con la contraseña nueva | `POST /api/auth/login` | **200**, confirma que el cambio se guardó de verdad |
| Ver planes (sin sesión) | `GET /api/planes` | **200**, lista pública accesible sin token |
| Crear un plan sin ser ADMIN | `POST /api/planes` | **403**, la seguridad por rol sigue intacta |

Todas las respuestas coincidieron exactamente con lo que `api.js` espera
(mismos campos, mismos códigos de estado, mismo formato de error).

### Nota sobre el panel de administración contra el backend real

La base de datos de desarrollo (H2 en memoria) parte vacía en cada
reinicio y no trae ningún usuario ADMIN de fábrica — el registro público
siempre crea usuarios con rol `USUARIO`. Por eso el flujo de
administrador (crear/editar planes, cambiar roles, ver todas las
suscripciones) no se pudo ejercitar con una cuenta real en esta pasada
sin acceso directo a la base de datos para promover un usuario a ADMIN.
Ese flujo sí quedó validado exhaustivamente en la suite automatizada
(sección 1), que replica el contrato real endpoint por endpoint. Si
quieres, puedo promover un usuario tuyo a ADMIN (por ejemplo, a través
de la consola H2 en `/h2-console`, o crear un endpoint/semilla de datos
para tu primer administrador) y repetir esa parte contra el backend real.

## 3. Prueba real de interfaz (clics de verdad contra el backend real)

Además de la suite simulada y de probar los endpoints por separado, se
hizo una prueba de integración completa de verdad: el propio backend
Spring Boot sirve ahora los archivos de BuildZone (copiados a
`src/main/resources/static/`), con un pequeño cambio en
`SecurityConfig.java` que permite el acceso público solo a esos archivos
estáticos (`Index.html`, `Style.css`, los `.js` y la carpeta
`validacion/`), sin tocar la protección de ninguna ruta `/api/**`. Esto
permite abrir `http://localhost:8080/Index.html` en un navegador real
—mismo origen que la API, sin CORS de por medio— e interactuar con la
interfaz exactamente como lo haría un usuario final.

Recorrido realizado con clics reales sobre la interfaz real, sin ningún
mock:

| Acción en la interfaz | Endpoint real detrás | Resultado |
|---|---|---|
| Abrir `http://localhost:8080/Index.html` | — (archivos estáticos servidos por Spring Boot) | Página cargada con todo su estilo y fondo, 0 errores de red en los assets |
| Registrar una cuenta nueva desde el modal | `POST /api/auth/registro` | **201**, sesión iniciada automáticamente, toast de bienvenida |
| Ver "Planes" ya autenticado | `GET /api/planes` | **200** (lista vacía: BD de pruebas sin planes sembrados, comportamiento esperado) |
| Abrir "Mi cuenta" → Perfil | `GET /api/usuarios/me` | **200**, formulario con los datos reales del usuario recién creado |
| Editar el nombre y guardar | `PUT /api/usuarios/me` | **200**, toast "Perfil actualizado correctamente" |
| Cambiar la contraseña en la pestaña Seguridad | `PUT /api/usuarios/me/password` | **204**, toast de confirmación |
| Cerrar sesión | — (limpieza de sesión local) | Toast "Sesión cerrada correctamente", nav vuelve a estado anido |
| Iniciar sesión de nuevo con la contraseña **nueva** | `POST /api/auth/login` | **200** — confirma que el cambio de contraseña quedó guardado de verdad en la base de datos |
| Intentar entrar al panel de administración (usuario sin rol ADMIN) | — (guard de `showPage('admin')` en `script.js`) | Bloqueado en el cliente con el toast "No tienes permisos de administrador" |
| Llamar `GET /api/usuarios` directamente por consola con el token de este usuario | `GET /api/usuarios` | **403** — confirma que la protección por rol también está activa en el backend, no solo en la interfaz |

Durante todo el recorrido no se registró ningún `pageerror` (excepción de
JavaScript no controlada); los únicos mensajes de tipo error en consola
corresponden a respuestas 401/403 esperadas de las pruebas negativas
(login con contraseña incorrecta, intento de acceso a `/api/usuarios`
sin permisos), no a fallos reales del código.

Esta prueba complementa a la sección 1 (que sí cubre el panel de
administrador completo, contra un backend simulado) con una verificación
de extremo a extremo real: mismo navegador, mismos clics, mismo backend
Spring Boot, misma base de datos H2, sin ningún mock de por medio.

## Conclusión

El frontend (BuildZone) y el backend (Spring Boot) hablan el mismo
contrato en ambos frentes: la lógica de la interfaz está probada a fondo
(33/33 en la suite simulada), los endpoints reales responden exactamente
como se espera, y una prueba real de clics de extremo a extremo —sirviendo
el propio frontend desde el backend— confirmó que todo el flujo de
cuenta de usuario (registro, edición de perfil, cambio de contraseña,
cierre e inicio de sesión) funciona correctamente contra la base de datos
real, y que la protección del panel de administrador funciona tanto en
la interfaz como en el backend. No se encontraron discrepancias.

# BuildZone: comparador de componentes de PC

Proyecto formativo SENA (Análisis y Desarrollo de Software). Es una aplicación web para
consultar y comparar componentes de PC (procesadores, tarjetas gráficas, memorias RAM...),
con cuentas de usuario, planes de suscripción y panel de administración.

## Estructura del repositorio

```
Proyecto Sena 1/
├── index.html, Style.css           Front-end (SPA sin framework, HTML + CSS + JavaScript)
├── config.js      URL de la API según el ambiente (desarrollo / producción)
├── ui.js          Utilidades de interfaz: toasts, formato de moneda, escaparHtml (anti-XSS)
├── api.js         Capa de acceso al backend: fetch + JWT + manejo de errores HTTP
├── auth.js        Login, registro, cierre de sesión y menú de usuario
├── planes.js      Página pública de planes y suscripción
├── cuenta.js      Mi cuenta: perfil, contraseña, suscripción e historial
├── admin.js       Panel admin: usuarios, planes y suscripciones
├── catalogo.js    Catálogo desde la API + pestaña Catálogo del panel admin
├── script.js      Navegación entre páginas, productos, comparador, blog, contacto
├── backend-api/   API REST Spring Boot (catálogo, seguridad, usuarios, planes, suscripciones)
├── backend-java/  Módulo de consola JDBC (CRUD de catálogo), etapa anterior del proyecto
├── validacion/    Pruebas E2E de la interfaz con Playwright
├── scripts/       Scripts .bat/.sh para iniciar, probar y construir el JAR
├── render.yaml    Despliegue de la API en Render (Blueprint)
└── .github/workflows/  CI (pruebas en cada push) y Release (publica el JAR)
```

## Arquitectura

```
 Navegador (index.html + módulos JS)
        │  HTTP/JSON  ·  Authorization: Bearer <JWT>
        ▼
 backend-api (Spring Boot, puerto 8080)
   ├─ Seguridad ........ Spring Security + filtro JWT (transversal)
   ├─ Presentación ..... controller/   (REST, validación de DTOs)
   ├─ Negocio .......... service/impl/ (reglas, transacciones)
   └─ Datos ............ repository/   (Spring Data JPA / Hibernate)
        │  JDBC
        ▼
 MySQL/MariaDB "buildzone" (XAMPP)   ·   H2 en memoria (dev / pruebas)
        ▲
 backend-java (consola JDBC) ── usa las mismas tablas producto, marca, categoria
```

El detalle de paquetes, patrones de diseño, seguridad y endpoints está en
[`backend-api/README.md`](backend-api/README.md).

### Librerías y frameworks por capa

| Capa | Front-end | Back-end |
|---|---|---|
| Presentación | HTML5, CSS3, JavaScript (ES2020), Fetch API | Spring Web, Jackson |
| Seguridad | JWT guardado en `localStorage`, escape de HTML | Spring Security 6, JJWT, BCrypt |
| Negocio | Módulos JS por página | Spring (servicios, `@Transactional`), Bean Validation |
| Datos | — | Spring Data JPA, Hibernate, MySQL Connector/J, H2 |
| Pruebas | Playwright (E2E) | JUnit 5, Mockito, AssertJ, MockMvc |

## Mapa de navegación

```
Inicio ─┬─ Productos (filtro por categoría + buscador)
        ├─ Comparador (duelo entre 2 productos de la misma categoría)
        ├─ Blog (modal de artículo)
        ├─ Planes ── Suscribirme (requiere sesión)
        ├─ Contacto
        ├─ [👤] Iniciar sesión ⇄ Registrarse
        └─ (con sesión) Menú de usuario
              ├─ Mi cuenta: Perfil · Seguridad (contraseña) · Suscripción (actual + historial)
              ├─ Panel admin (solo ADMIN): Usuarios · Planes · Suscripciones · Catálogo
              └─ Cerrar sesión
```

## Ambientes

| | Desarrollo rápido | Desarrollo con XAMPP | Pruebas automatizadas |
|---|---|---|---|
| Perfil Spring | `dev` | *(por defecto)* | `test` |
| Base de datos | H2 en memoria | MySQL/MariaDB `buildzone` | H2 en memoria |
| Esquema | Lo crea Hibernate | Scripts en `backend-api/database/mysql` | Lo crea Hibernate |
| Datos iniciales | Admin + planes + catálogo de ejemplo | Admin + planes (el catálogo es el real) | Admin + planes |
| Comando | `mvn spring-boot:run -Dspring-boot.run.profiles=dev` | `mvn spring-boot:run` | `mvn test` |

### Requisitos del equipo de desarrollo

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK | 21 | Compilar y ejecutar `backend-api` y `backend-java` |
| Maven | 3.9+ (o el que trae NetBeans/IntelliJ) | Dependencias, build y pruebas |
| XAMPP (MariaDB 10.4+) o MySQL 8 | — | Base de datos `buildzone` (opcional si se usa el perfil `dev`) |
| Navegador moderno | Chrome, Edge o Firefox | Front-end |
| VS Code + Live Server (opcional) | — | Servir el front-end en `http://127.0.0.1:5500` |
| Node.js 18+ y Playwright (opcional) | — | Pruebas E2E de `validacion/` |
| Git | 2.x | Control de versiones |

### Servidores y puertos

| Servicio | Puerto | Configuración |
|---|---|---|
| backend-api (Tomcat embebido) | 8080 | `server.port` en `application.properties` |
| MySQL/MariaDB (XAMPP) | 3306 | `spring.datasource.url` |
| Front-end con Live Server | 5500 | CORS permitido por `buildzone.cors.origenes-permitidos` |

## Cómo ejecutar todo

1. **Backend**, en una terminal:
   ```bash
   cd backend-api
   mvn spring-boot:run -Dspring-boot.run.profiles=dev     # sin XAMPP
   # o, con XAMPP y el script 01 ya ejecutado:
   mvn spring-boot:run
   ```
2. **Front-end**: abra `index.html` con Live Server o con doble clic.
3. Ingrese con `admin@buildzone.com` / `Admin1234` para ver el panel de administración,
   o cree una cuenta nueva desde "Regístrate".

Si el backend está apagado, las páginas Productos y Comparador siguen funcionando con los
datos locales de `script.js`, y las funciones de cuenta muestran un aviso de conexión.

## Despliegue en la nube

| Módulo | Plataforma | URL |
|---|---|---|
| Front-end | GitHub Pages | `https://<usuario>.github.io/<repositorio>/` |
| API REST | Render (Docker, plan gratuito) | `https://<servicio>.onrender.com/api` |

1. **API en Render**: *New → Blueprint*, seleccione este repositorio. Render lee `render.yaml`,
   construye `backend-api/Dockerfile` y arranca con el perfil `prod` (H2 en memoria con datos de
   ejemplo). Defina `ADMIN_PASSWORD` cuando lo pida.
2. **Front-end en GitHub Pages**: *Settings → Pages → Deploy from a branch → main / (root)*.
3. Copie la URL de Render en `API_URL_PRODUCCION` de `config.js`, haga commit y push.

El plan gratuito de Render apaga el servicio tras 15 minutos sin uso; la primera petición
después de eso tarda alrededor de un minuto mientras vuelve a arrancar.

## Ejecutables

- `scripts\construir-jar.bat` genera `backend-api/target/buildzone-api-1.0.0.jar`.
- `scripts\ejecutar-jar.bat` lo ejecuta (`java -jar buildzone-api-1.0.0.jar --spring.profiles.active=dev`).
- Al publicar una etiqueta (`git tag v1.0.0 && git push origin v1.0.0`), GitHub Actions crea un
  *Release* con el JAR, el front-end empaquetado y los scripts SQL.

## Pruebas

| Nivel | Dónde | Comando |
|---|---|---|
| Unitarias + integración (backend, 59) | `backend-api/src/test` | `cd backend-api && mvn test` |
| End-to-end (interfaz, 47) | `validacion/e2e_buildzone.mjs` | `cd validacion && npm install && npm test` |

Ambas suites se ejecutan automáticamente en GitHub Actions en cada push (`.github/workflows/ci.yml`).

## Control de versiones

Repositorio Git único con commits pequeños por módulo, con prefijo según el tipo de cambio:
`feat:` funcionalidad, `fix:` corrección, `test:` pruebas, `docs:` documentación,
`refactor:` reorganización sin cambio de comportamiento. Ver `git log --oneline`.

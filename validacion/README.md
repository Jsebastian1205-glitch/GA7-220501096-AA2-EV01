# Validación E2E de BuildZone

`e2e_buildzone.mjs` valida el sitio completo (Playwright + Chromium) contra un
backend simulado que respeta el contrato real de la API Spring Boot
(rutas, verbos, forma de los DTOs y códigos de estado — tomados
directamente de los controladores/DTOs del backend en
`pc-compare-gestion-usuarios`).

Requiere Playwright instalado globalmente (`npm i -g playwright`, con
Chromium descargado). Para ejecutarlo:

```bash
cd validacion
ln -sf "$(npm root -g)" node_modules   # si playwright está instalado global
node e2e_buildzone.mjs
```

Si Chromium no está en la ruta por defecto de Playwright, indíquela con la
variable `CHROMIUM_PATH`.

Cubre: catálogo cargado desde la API (y respaldo local si la API está
caída), navegación entre páginas, buscador y comparador de productos,
modal de artículos, formulario de contacto, registro y login (casos
correctos e incorrectos), planes y suscripción (alta y cancelación),
Mi cuenta (perfil, contraseña, historial), y el panel de administración
(usuarios, planes, suscripciones y catálogo: crear marca/categoría,
crear/editar/eliminar producto), y la protección anti-XSS al mostrar datos
de usuarios — 47 verificaciones en total.

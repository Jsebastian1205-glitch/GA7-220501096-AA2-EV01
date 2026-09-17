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

Cubre: navegación entre páginas, buscador y comparador de productos,
modal de artículos, formulario de contacto, registro y login (casos
correctos e incorrectos), planes y suscripción (alta y cancelación),
Mi cuenta (perfil, contraseña, historial), y el panel de administración
(usuarios, planes, suscripciones) — 33 verificaciones en total.

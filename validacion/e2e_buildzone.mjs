// Validación E2E de BuildZone contra un backend simulado (mock) que
// respeta EXACTAMENTE el contrato real de la API Spring Boot (rutas,
// verbos, forma de los DTOs de request/response, y códigos de estado),
// tal como se documentó al leer los controladores/DTOs reales del
// backend en pc-compare-gestion-usuarios.
//
// Sirve la carpeta del proyecto (la carpeta padre de validacion/) como
// estático en localhost:8010 e intercepta todas las llamadas a
// http://localhost:8080/api/** con context.route().
//
// Incluye el módulo de catálogo (productos, marcas, categorías) con los
// mismos DTOs de ProductoResponse/MarcaResponse/CategoriaResponse.

import http from 'node:http';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { chromium } from 'playwright';

const SITE_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const SITE_PORT = 8010;

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
};

function iniciarServidorEstatico() {
  return new Promise((resolve) => {
    const server = http.createServer(async (req, res) => {
      let urlPath = decodeURIComponent(req.url.split('?')[0]);
      if (urlPath === '/') urlPath = '/index.html';
      const filePath = path.join(SITE_DIR, urlPath);
      try {
        const contenido = await readFile(filePath);
        const ext = path.extname(filePath);
        res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
        res.end(contenido);
      } catch {
        res.writeHead(404);
        res.end('Not found');
      }
    });
    server.listen(SITE_PORT, () => resolve(server));
  });
}

// ── Estado del backend simulado ──
let siguienteId = 100;
const usuarios = new Map([
  [1, { id: 1, nombre: 'Ana', apellido: 'Gómez', username: 'ana.gomez', email: 'ana@buildzone.com', rol: 'USUARIO', estado: 'ACTIVO', password: 'Secreta123', fechaRegistro: '2026-01-10T10:00:00' }],
  [2, { id: 2, nombre: 'Root', apellido: 'Admin', username: 'admin', email: 'admin@buildzone.com', rol: 'ADMIN', estado: 'ACTIVO', password: 'Admin1234', fechaRegistro: '2026-01-01T09:00:00' }],
  // Usuario con HTML en el nombre: el panel admin debe mostrarlo como texto (anti-XSS).
  [3, { id: 3, nombre: '<img src=x id=xss-inyectado>', apellido: 'Malicioso', username: 'hacker', email: 'hacker@test.com', rol: 'USUARIO', estado: 'ACTIVO', password: 'Hacker123', fechaRegistro: '2026-02-01T09:00:00' }],
]);

// ── Catálogo simulado (mismo contenido que CatalogoDemoConfig del perfil dev) ──
const categorias = new Map([
  [1, { idCategoria: 1, nombre: 'Procesador', descripcion: null }],
  [2, { idCategoria: 2, nombre: 'Tarjeta Grafica', descripcion: null }],
  [3, { idCategoria: 3, nombre: 'Memoria RAM', descripcion: null }],
]);
const marcas = new Map(
  ['Intel', 'AMD', 'NVIDIA', 'Kingston', 'Corsair', 'G.Skill'].map((nombre, i) => [i + 1, { idMarca: i + 1, nombre, descripcion: null }])
);
const idMarca = (nombre) => [...marcas.values()].find((m) => m.nombre === nombre).idMarca;
const productos = new Map(
  [
    [1, 'Intel', 'Core i5-14600K'], [1, 'Intel', 'Core i7-14700K'], [1, 'Intel', 'Core i9-14900K'],
    [1, 'AMD', 'Ryzen 5 7600X'], [1, 'AMD', 'Ryzen 7 7700X'],
    [2, 'NVIDIA', 'RTX 4060'], [2, 'NVIDIA', 'RTX 4070'], [2, 'NVIDIA', 'RTX 4070 Ti'],
    [2, 'AMD', 'Radeon RX 7600'], [2, 'AMD', 'Radeon RX 7800 XT'],
    [3, 'Kingston', 'Fury Beast 16GB 3200MHz'], [3, 'Corsair', 'Vengeance 32GB 6000MHz'],
    [3, 'G.Skill', 'Trident Z5 32GB 6400MHz'], [3, 'Corsair', 'Dominator Platinum 64GB 5600MHz'],
  ].map(([idCategoria, marca, nombre], i) => [
    i + 1,
    { idProducto: i + 1, nombre, descripcion: `Descripcion API de ${nombre}`, imagen: null, idMarca: idMarca(marca), idCategoria },
  ])
);
function respuestaProducto(p) {
  return {
    ...p,
    nombreMarca: marcas.get(p.idMarca).nombre,
    nombreCategoria: categorias.get(p.idCategoria).nombre,
  };
}
const planes = new Map([
  [1, { id: 1, nombre: 'Gratuito', descripcion: 'Acceso básico al comparador.', precio: 0, duracionDias: 3650, activo: true }],
  [2, { id: 2, nombre: 'Premium', descripcion: 'Comparaciones ilimitadas y soporte prioritario.', precio: 29900, duracionDias: 30, activo: true }],
]);
const suscripciones = new Map(); // id -> {id, usuarioId, planId, fechaInicio, fechaFin, estado}
let tokenUsuarioMap = { 'token-ana': 1, 'token-admin': 2 };

function respuestaUsuario(u) {
  return { id: u.id, nombre: u.nombre, apellido: u.apellido, username: u.username, email: u.email, rol: u.rol, estado: u.estado, fechaRegistro: u.fechaRegistro };
}
function respuestaPlan(p) {
  return { id: p.id, nombre: p.nombre, descripcion: p.descripcion, precio: p.precio, duracionDias: p.duracionDias, activo: p.activo };
}
function respuestaSuscripcion(s) {
  return { id: s.id, usuarioId: s.usuarioId, plan: respuestaPlan(planes.get(s.planId)), fechaInicio: s.fechaInicio, fechaFin: s.fechaFin, estado: s.estado };
}
function usuarioDesdeToken(headers) {
  const auth = headers['authorization'] || '';
  const token = auth.replace('Bearer ', '');
  const id = tokenUsuarioMap[token];
  return id ? usuarios.get(id) : null;
}

async function manejarApi(route) {
  const req = route.request();
  const url = new URL(req.url());
  const ruta = url.pathname.replace(/^\/api/, '');
  const metodo = req.method();
  const headers = req.headers();
  let body = {};
  try { body = req.postDataJSON() || {}; } catch { body = {}; }

  const json = (status, data) => route.fulfill({ status, contentType: 'application/json', body: data === undefined ? '' : JSON.stringify(data) });
  const error = (status, mensaje, detalles) => json(status, { timestamp: new Date().toISOString(), status, error: 'Error', mensaje, ruta, detalles });

  // ── Catálogo: GET público, escritura solo ADMIN ──
  if (ruta.startsWith('/productos') || ruta.startsWith('/marcas') || ruta.startsWith('/categorias')) {
    const lista = ruta.startsWith('/productos') ? productos : ruta.startsWith('/marcas') ? marcas : categorias;
    const campoId = ruta.startsWith('/productos') ? 'idProducto' : ruta.startsWith('/marcas') ? 'idMarca' : 'idCategoria';
    const aDto = ruta.startsWith('/productos') ? respuestaProducto : (x) => x;
    const matchId = ruta.match(/^\/\w+\/(\d+)$/);

    if (metodo === 'GET' && !matchId) return json(200, [...lista.values()].map(aDto));
    if (metodo === 'GET' && matchId) {
      const item = lista.get(Number(matchId[1]));
      return item ? json(200, aDto(item)) : error(404, 'No existe');
    }
    const actorCatalogo = usuarioDesdeToken(headers);
    if (!actorCatalogo) return error(401, 'Debes iniciar sesion para realizar esta accion.');
    if (actorCatalogo.rol !== 'ADMIN') return error(403, 'No tienes permisos para realizar esta accion.');
    if (!body.nombre && metodo !== 'DELETE') return error(400, 'Los datos enviados no son validos.', ['El nombre es obligatorio']);

    if (metodo === 'POST') {
      if (!ruta.startsWith('/productos') && [...lista.values()].some((x) => x.nombre.toLowerCase() === body.nombre.toLowerCase())) {
        return error(409, `Ya existe un registro con el nombre '${body.nombre}'`);
      }
      const id = siguienteId++;
      const nuevo = { ...body, [campoId]: id };
      lista.set(id, nuevo);
      return json(201, aDto(nuevo));
    }
    if (metodo === 'PUT' && matchId) {
      const item = lista.get(Number(matchId[1]));
      Object.assign(item, body);
      return json(200, aDto(item));
    }
    if (metodo === 'DELETE' && matchId) {
      lista.delete(Number(matchId[1]));
      return json(204);
    }
  }

  // ── /auth ──
  if (ruta === '/auth/login' && metodo === 'POST') {
    const u = [...usuarios.values()].find((x) => x.email === body.identificador || x.username === body.identificador);
    if (!u || u.password !== body.password) return error(401, 'Credenciales inválidas.');
    if (u.estado === 'INACTIVO') return error(403, 'Tu cuenta está inactiva.');
    const token = u.rol === 'ADMIN' ? 'token-admin' : `token-${u.username}`;
    tokenUsuarioMap[token] = u.id;
    return json(200, { token, tipo: 'Bearer', usuario: respuestaUsuario(u) });
  }
  if (ruta === '/auth/registro' && metodo === 'POST') {
    if ([...usuarios.values()].some((x) => x.email === body.email)) return error(400, 'El correo ya está registrado.');
    const id = siguienteId++;
    const nuevo = { id, nombre: body.nombre, apellido: body.apellido, username: body.username, email: body.email, rol: 'USUARIO', estado: 'ACTIVO', password: body.password, fechaRegistro: new Date().toISOString() };
    usuarios.set(id, nuevo);
    const token = `token-${nuevo.username}`;
    tokenUsuarioMap[token] = id;
    return json(201, { token, tipo: 'Bearer', usuario: respuestaUsuario(nuevo) });
  }

  // Requiere sesión para todo lo demás salvo GET /planes
  const actor = usuarioDesdeToken(headers);

  if (ruta === '/usuarios/me' && metodo === 'GET') {
    if (!actor) return error(401, 'No autenticado.');
    return json(200, respuestaUsuario(actor));
  }
  if (ruta === '/usuarios/me' && metodo === 'PUT') {
    if (!actor) return error(401, 'No autenticado.');
    actor.nombre = body.nombre; actor.apellido = body.apellido; actor.email = body.email;
    return json(200, respuestaUsuario(actor));
  }
  if (ruta === '/usuarios/me/password' && metodo === 'PUT') {
    if (!actor) return error(401, 'No autenticado.');
    if (actor.password !== body.passwordActual) return error(400, 'La contraseña actual no es correcta.');
    actor.password = body.passwordNueva;
    return json(204);
  }
  if (ruta === '/usuarios' && metodo === 'GET') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    return json(200, [...usuarios.values()].map(respuestaUsuario));
  }
  const matchRol = ruta.match(/^\/usuarios\/(\d+)\/rol$/);
  if (matchRol && metodo === 'PATCH') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    const u = usuarios.get(Number(matchRol[1]));
    u.rol = body.rol;
    return json(200, respuestaUsuario(u));
  }
  const matchEstado = ruta.match(/^\/usuarios\/(\d+)\/estado$/);
  if (matchEstado && metodo === 'PATCH') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    const u = usuarios.get(Number(matchEstado[1]));
    u.estado = body.estado;
    return json(200, respuestaUsuario(u));
  }

  if (ruta === '/planes' && metodo === 'GET') {
    return json(200, [...planes.values()].filter((p) => p.activo).map(respuestaPlan));
  }
  if (ruta === '/planes/todos' && metodo === 'GET') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    return json(200, [...planes.values()].map(respuestaPlan));
  }
  if (ruta === '/planes' && metodo === 'POST') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    const id = siguienteId++;
    const nuevo = { id, nombre: body.nombre, descripcion: body.descripcion, precio: body.precio, duracionDias: body.duracionDias, activo: true };
    planes.set(id, nuevo);
    return json(201, respuestaPlan(nuevo));
  }
  const matchPlanId = ruta.match(/^\/planes\/(\d+)$/);
  if (matchPlanId && metodo === 'PUT') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    const p = planes.get(Number(matchPlanId[1]));
    Object.assign(p, { nombre: body.nombre, descripcion: body.descripcion, precio: body.precio, duracionDias: body.duracionDias });
    return json(200, respuestaPlan(p));
  }
  if (matchPlanId && metodo === 'DELETE') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    planes.get(Number(matchPlanId[1])).activo = false;
    return json(204);
  }

  if (ruta === '/suscripciones' && metodo === 'POST') {
    if (!actor) return error(401, 'No autenticado.');
    const id = siguienteId++;
    const nueva = { id, usuarioId: actor.id, planId: body.planId, fechaInicio: '2026-09-13', fechaFin: '2026-10-13', estado: 'ACTIVA' };
    suscripciones.set(id, nueva);
    return json(201, respuestaSuscripcion(nueva));
  }
  if (ruta === '/suscripciones/me/activa' && metodo === 'GET') {
    if (!actor) return error(401, 'No autenticado.');
    const activa = [...suscripciones.values()].find((s) => s.usuarioId === actor.id && s.estado === 'ACTIVA');
    if (!activa) return json(204);
    return json(200, respuestaSuscripcion(activa));
  }
  if (ruta === '/suscripciones/me' && metodo === 'GET') {
    if (!actor) return error(401, 'No autenticado.');
    return json(200, [...suscripciones.values()].filter((s) => s.usuarioId === actor.id).map(respuestaSuscripcion));
  }
  const matchCancelar = ruta.match(/^\/suscripciones\/(\d+)$/);
  if (matchCancelar && metodo === 'DELETE') {
    if (!actor) return error(401, 'No autenticado.');
    const s = suscripciones.get(Number(matchCancelar[1]));
    if (s) s.estado = 'CANCELADA';
    return json(204);
  }
  if (ruta === '/suscripciones' && metodo === 'GET') {
    if (!actor || actor.rol !== 'ADMIN') return error(403, 'No tiene permisos.');
    return json(200, [...suscripciones.values()].map(respuestaSuscripcion));
  }

  return error(404, `Ruta simulada no encontrada: ${metodo} ${ruta}`);
}

const resultados = [];
function check(nombre, condicion) {
  resultados.push({ nombre, ok: !!condicion });
  console.log(`${condicion ? '✅' : '❌'} ${nombre}`);
}

async function main() {
  const server = await iniciarServidorEstatico();
  // CHROMIUM_PATH permite indicar un Chromium concreto; si no, usa el de Playwright.
  const browser = await chromium.launch(process.env.CHROMIUM_PATH ? { executablePath: process.env.CHROMIUM_PATH } : {});

  // ── Degradación elegante: con la API caída, el catálogo local sigue funcionando ──
  const contextoSinApi = await browser.newContext();
  await contextoSinApi.route('http://localhost:8080/api/**', (route) => route.abort());
  const paginaSinApi = await contextoSinApi.newPage();
  const erroresSinApi = [];
  paginaSinApi.on('pageerror', (err) => erroresSinApi.push(err.message));
  await paginaSinApi.goto(`http://localhost:${SITE_PORT}/index.html`);
  await paginaSinApi.click('#nav-productos');
  await paginaSinApi.waitForTimeout(400);
  check('Sin backend, Productos muestra el catálogo local de respaldo (14)', await paginaSinApi.locator('.product-card').count() === 14);
  check('Sin backend, no hay excepciones de JS', erroresSinApi.length === 0);
  await contextoSinApi.close();

  const context = await browser.newContext();
  const page = await context.newPage();

  // Solo nos interesan los errores de JS reales (excepciones no capturadas).
  // Los "Failed to load resource: ... 401/400" son ruido esperado del propio
  // navegador cuando probamos deliberadamente credenciales/datos inválidos
  // contra el mock (son respuestas HTTP de error manejadas por api.js, no
  // bugs), y ERR_TUNNEL_CONNECTION_FAILED es telemetría de Chrome hacia
  // dominios de Google bloqueados por el proxy de este sandbox, ajena al
  // sitio. Un pageerror (excepción no capturada) sí sería un bug real.
  const erroresJS = [];
  page.on('pageerror', (err) => erroresJS.push(`pageerror: ${err.message}`));

  await context.route('http://localhost:8080/api/**', manejarApi);

  await page.goto(`http://localhost:${SITE_PORT}/index.html`);
  await page.waitForSelector('#page-inicio.active');

  check('La página de inicio carga activa', await page.locator('#page-inicio.active').count() === 1);
  check('El nav de Inicio está activo', await page.locator('#nav-inicio.active').count() === 1);
  check('El icono de login es visible (sin sesión)', await page.locator('#nav-login-btn').isVisible());
  check('El enlace Admin está oculto (sin sesión)', !(await page.locator('#nav-admin').isVisible()));

  // ── Navegación básica ──
  await page.click('#nav-productos');
  await page.waitForSelector('#page-productos.active');
  await page.waitForSelector('.product-card-desc');
  const productCount = await page.locator('.product-card').count();
  check('La página de Productos muestra los productos de la API (14)', productCount === 14);
  check('Las tarjetas muestran la descripción que viene de la API', await page.locator('.product-card-desc', { hasText: /^Descripcion API de RTX 4070$/ }).count() === 1);
  check('Los productos de la API conservan el precio de referencia', await page.locator('.product-card', { hasText: 'RTX 4070 Ti' }).locator('.product-card-price').textContent().then((t) => t.includes('3.600.000')));

  await page.click('#products-categories [data-category="gpu"]');
  await page.waitForTimeout(150);
  check('El filtro por categoría (Tarjetas Gráficas) usa la categoría de la BD', await page.locator('.product-card').count() === 5);
  await page.click('#products-categories [data-category="all"]');

  await page.fill('#products-search', 'ryzen');
  await page.waitForTimeout(150);
  const filtrados = await page.locator('.product-card').count();
  check('El buscador de productos filtra resultados', filtrados === 2);
  await page.fill('#products-search', '');

  await page.click('#nav-comparador');
  await page.waitForSelector('#page-comparador.active');
  await page.selectOption('#duel-select-a', { index: 1 });
  await page.selectOption('#duel-select-b', { index: 2 });
  await page.waitForTimeout(150);
  check('El comparador (duelo) renderiza 2 tarjetas', await page.locator('.duel-card').count() === 2);

  await page.click('#nav-blog');
  await page.waitForTimeout(400);
  check('El enlace Blog no rompe la página (sigue en inicio)', await page.locator('#page-inicio.active').count() === 1);

  await page.click('[data-article="cpu-guide"]');
  await page.waitForSelector('#article-modal.active');
  check('El modal de artículo abre correctamente', await page.locator('#article-title').textContent().then((t) => t.includes('procesador')));
  await page.click('#article-modal .modal-close');
  await page.waitForTimeout(200);
  check('El modal de artículo cierra correctamente', await page.locator('#article-modal.active').count() === 0);

  await page.click('#nav-contacto');
  await page.waitForSelector('#page-contacto.active');
  await page.fill('#page-contacto input[type="text"]', 'Juan Tester');
  await page.fill('#page-contacto input[type="email"]', 'juan@test.com');
  await page.fill('#page-contacto textarea', 'Mensaje de prueba');
  await page.click('#contacto-form button[type="submit"]');
  await page.waitForSelector('.toast-exito');
  check('El formulario de contacto muestra un toast de éxito', true);

  // ── Registro (con bug de confirmación de contraseña) ──
  await page.click('#nav-inicio');
  await page.click('#nav-login-btn');
  await page.waitForSelector('#login-modal.active');
  await page.click('[data-open-register]');
  await page.waitForSelector('#register-modal.active');
  check('El link "Regístrate" abre el modal de registro (bug de toggleRegister corregido)', true);

  await page.fill('#register-name', 'Carlos Ruiz');
  await page.fill('#register-email', 'carlos@test.com');
  await page.fill('#register-password', 'Password123');
  await page.fill('#register-password-confirm', 'OtraCosa123');
  await page.click('#register-form button[type="submit"]');
  await page.waitForSelector('.toast-error');
  check('Registro con contraseñas distintas muestra error (validación cliente)', true);

  await page.fill('#register-password-confirm', 'Password123');
  await page.click('#register-form button[type="submit"]');
  await page.waitForSelector('#user-menu-btn');
  check('Registro válido inicia sesión automáticamente (aparece el menú de usuario)', true);
  await page.click('#user-menu-btn');
  await page.click('[data-action="logout"]');
  await page.waitForSelector('#nav-login-btn:visible');

  // ── Login incorrecto y correcto ──
  await page.click('#nav-login-btn');
  await page.waitForSelector('#login-modal.active');
  await page.fill('#login-email', 'ana@buildzone.com');
  await page.fill('#login-password', 'clave-incorrecta');
  await page.click('#login-form button[type="submit"]');
  await page.waitForSelector('.toast-error');
  check('Login con credenciales incorrectas muestra un toast de error', true);

  await page.fill('#login-password', 'Secreta123');
  await page.click('#login-form button[type="submit"]');
  await page.waitForSelector('#user-menu-btn');
  const nombreUsuario = await page.locator('.user-name').textContent();
  check('Login correcto muestra el menú de usuario con el username', nombreUsuario.trim() === 'ana.gomez');
  check('El modal de login se cerró tras el login', await page.locator('#login-modal.active').count() === 0);

  // ── Planes (pública, ahora autenticado) ──
  await page.click('#nav-planes');
  await page.waitForSelector('.plan-card');
  check('La página de Planes muestra las tarjetas de planes', await page.locator('.plan-card').count() === 2);

  await page.click('.plan-card:has-text("Premium") button');
  await page.waitForSelector('.toast-exito');
  await page.waitForTimeout(300);
  check('Suscribirse a un plan muestra éxito y lo marca como plan actual', await page.locator('.plan-card-current:has-text("Premium")').count() === 1);

  // ── Mi cuenta ──
  await page.click('#user-menu-btn');
  await page.click('[data-action="cuenta"]');
  await page.waitForSelector('#page-cuenta.active');
  await page.waitForTimeout(300);
  check('Mi cuenta precarga el nombre de usuario', await page.locator('#perfil-username').inputValue().then((v) => v === 'ana.gomez'));

  await page.click('[data-tab="suscripcion"]');
  await page.waitForTimeout(200);
  check('La pestaña de suscripción muestra el plan Premium activo', await page.locator('#suscripcion-actual .subscription-card-plan').textContent().then((t) => t.includes('Premium')));

  await page.click('#btn-cancelar-suscripcion');
  await page.waitForSelector('.toast-exito');
  await page.waitForTimeout(300);
  check('Cancelar la suscripción muestra éxito y refresca el estado', await page.locator('#suscripcion-actual').textContent().then((t) => t.includes('no tienes una suscripción activa')));

  await page.click('[data-tab="seguridad"]');
  await page.fill('#password-actual', 'clave-mala');
  await page.fill('#password-nueva', 'NuevaClave123');
  await page.click('#password-form button[type="submit"]');
  await page.waitForSelector('.toast-error');
  check('Cambiar contraseña con la actual incorrecta muestra error', true);

  await page.fill('#password-actual', 'Secreta123');
  await page.click('#password-form button[type="submit"]');
  await page.waitForSelector('.toast-exito');
  check('Cambiar contraseña con datos correctos muestra éxito', true);

  await page.click('[data-tab="perfil"]');
  await page.fill('#perfil-nombre', 'Ana María');
  await page.click('#perfil-form button[type="submit"]');
  await page.waitForSelector('.toast-exito');
  check('Actualizar el perfil muestra éxito', true);

  await page.click('#user-menu-btn');
  await page.click('[data-action="logout"]');
  await page.waitForSelector('#nav-login-btn:visible');
  check('Cerrar sesión vuelve a mostrar el icono de login', true);

  // ── Panel de administración ──
  await page.click('#nav-login-btn');
  await page.fill('#login-email', 'admin@buildzone.com');
  await page.fill('#login-password', 'Admin1234');
  await page.click('#login-form button[type="submit"]');
  await page.waitForSelector('#user-menu-btn');
  check('El enlace Admin aparece para un usuario con rol ADMIN', await page.locator('#nav-admin').isVisible());

  await page.click('#nav-admin');
  await page.waitForSelector('#page-admin.active');
  await page.waitForTimeout(300);
  check('El panel admin lista usuarios', await page.locator('#admin-usuarios-table tbody tr').count() >= 2);

  await page.click('[data-admin-tab="planes"]');
  await page.waitForTimeout(200);
  check('El panel admin lista planes (tab Planes)', await page.locator('#admin-planes-table tbody tr').count() === 2);

  await page.click('#btn-nuevo-plan');
  await page.waitForSelector('#plan-modal.active');
  await page.fill('#plan-nombre', 'Plan Estudiante');
  await page.fill('#plan-descripcion', 'Descuento para estudiantes');
  await page.fill('#plan-precio', '9900');
  await page.fill('#plan-duracion', '30');
  await page.click('#plan-form button[type="submit"]');
  await page.waitForSelector('.toast-exito');
  await page.waitForTimeout(300);
  check('Crear un plan nuevo lo agrega a la tabla', await page.locator('#admin-planes-table tbody tr').count() === 3);

  await page.click('[data-admin-tab="usuarios"]');
  await page.waitForTimeout(200);
  const filaAna = page.locator('#admin-usuarios-table tbody tr', { hasText: 'ana.gomez' });
  await filaAna.locator('[data-rol-usuario-id]').click();
  await page.waitForSelector('.toast-exito');
  await page.waitForTimeout(300);
  check('Cambiar el rol de un usuario se refleja en la tabla', await page.locator('#admin-usuarios-table tbody tr', { hasText: 'ana.gomez' }).locator('.badge-purple').count() === 1);

  await page.click('[data-admin-tab="suscripciones"]');
  await page.waitForTimeout(200);
  check('El panel admin carga la tabla de suscripciones sin errores', await page.locator('#admin-suscripciones-table').isVisible());

  check('Un nombre con HTML se muestra como texto y no se inyecta (anti-XSS)',
    (await page.locator('#xss-inyectado').count()) === 0 &&
    (await page.locator('#admin-usuarios-table', { hasText: '<img src=x id=xss-inyectado>' }).count()) === 1);

  // ── Panel admin: Catálogo ──
  await page.click('[data-admin-tab="catalogo"]');
  await page.waitForTimeout(200);
  check('La pestaña Catálogo lista los 14 productos de la API', await page.locator('#admin-catalogo-table tbody tr').count() === 14);

  await page.fill('#nueva-marca-nombre', 'Seasonic');
  await page.click('#btn-nueva-marca');
  await page.waitForSelector('.toast-exito:has-text("Seasonic")');
  await page.fill('#nueva-categoria-nombre', 'Fuente de Poder');
  await page.click('#btn-nueva-categoria');
  await page.waitForSelector('.toast-exito:has-text("Fuente de Poder")');
  check('Se crean una marca y una categoría desde el panel', marcas.size === 7 && categorias.size === 4);

  await page.fill('#nueva-marca-nombre', 'Intel');
  await page.click('#btn-nueva-marca');
  await page.waitForSelector('.toast-error:has-text("Ya existe")');
  check('Una marca repetida muestra el error 409 del backend', true);

  await page.click('#btn-nuevo-producto');
  await page.waitForSelector('#producto-modal.active');
  await page.fill('#producto-nombre', 'Focus GX-750');
  await page.fill('#producto-descripcion', '750 W, 80 Plus Gold');
  await page.selectOption('#producto-marca', { label: 'Seasonic' });
  await page.selectOption('#producto-categoria', { label: 'Fuente de Poder' });
  await page.click('#producto-form-submit');
  await page.waitForSelector('.toast-exito:has-text("Producto creado")');
  await page.waitForTimeout(300);
  check('Crear un producto lo agrega a la tabla del catálogo', await page.locator('#admin-catalogo-table tbody tr').count() === 15);

  const filaNueva = page.locator('#admin-catalogo-table tbody tr', { hasText: 'Focus GX-750' });
  await filaNueva.locator('[data-editar-producto-id]').click();
  await page.waitForSelector('#producto-modal.active');
  check('Editar precarga el formulario del producto', await page.locator('#producto-nombre').inputValue() === 'Focus GX-750');
  await page.fill('#producto-nombre', 'Focus GX-850');
  await page.click('#producto-form-submit');
  await page.waitForSelector('.toast-exito:has-text("Producto actualizado")');
  await page.waitForTimeout(300);
  check('Editar un producto actualiza la tabla', await page.locator('#admin-catalogo-table tbody tr', { hasText: 'Focus GX-850' }).count() === 1);

  await page.click('#nav-productos');
  await page.click('#products-categories [data-category="fuentes"]');
  await page.waitForTimeout(200);
  check('El producto nuevo aparece en Productos > Fuentes de Poder con "Precio por confirmar"',
    await page.locator('.product-card', { hasText: 'Focus GX-850' }).locator('.product-card-price').textContent().then((t) => t.includes('Precio por confirmar')));
  await page.click('#products-categories [data-category="all"]');

  await page.click('#nav-admin');
  await page.waitForSelector('#page-admin.active');
  await page.click('[data-admin-tab="catalogo"]');
  await page.waitForTimeout(300);
  page.once('dialog', (d) => d.accept());
  await page.locator('#admin-catalogo-table tbody tr', { hasText: 'Focus GX-850' }).locator('[data-eliminar-producto-id]').click();
  await page.waitForSelector('.toast-exito:has-text("Producto eliminado")');
  await page.waitForTimeout(300);
  check('Eliminar un producto lo quita de la tabla', await page.locator('#admin-catalogo-table tbody tr').count() === 14);

  check('No se registraron excepciones de JS (pageerror) en todo el recorrido', erroresJS.length === 0);
  if (erroresJS.length > 0) {
    console.log('Excepciones de JS detectadas:');
    erroresJS.forEach((e) => console.log('  -', e));
  }

  await browser.close();
  server.close();

  const fallidos = resultados.filter((r) => !r.ok);
  console.log(`\n${resultados.length - fallidos.length}/${resultados.length} verificaciones pasaron.`);
  if (fallidos.length > 0) {
    console.log('FALLARON:', fallidos.map((f) => f.nombre));
    process.exit(1);
  }
  process.exit(0);
}

main().catch((err) => {
  console.error('Error fatal en la validación:', err);
  process.exit(1);
});

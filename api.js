/**
 * Capa de acceso al backend real (Spring Boot — "Gestión de Usuarios y
 * Suscripciones"). Aísla el resto de los módulos (auth.js, planes.js,
 * cuenta.js, admin.js) de los detalles de fetch/JWT/errores HTTP: cada
 * módulo solo llama a una función con nombre en español que representa
 * la operación de negocio.
 *
 * Debe cargarse antes que auth.js/planes.js/cuenta.js/admin.js/script.js
 * en Index.html.
 */

// El backend corre en local durante el desarrollo (mvn spring-boot:run,
// puerto 8080 según application.yml). Si en algún momento se despliega en
// otra máquina, basta con cambiar esta constante.
const API_BASE_URL = 'http://localhost:8080/api';

const TOKEN_STORAGE_KEY = 'buildzone_token';
const USUARIO_STORAGE_KEY = 'buildzone_usuario';

/** Error enriquecido con el status HTTP y el cuerpo de error del backend. */
class ApiError extends Error {
  constructor(mensaje, status, data) {
    super(mensaje);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

function obtenerToken() {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

function obtenerUsuarioGuardado() {
  const crudo = localStorage.getItem(USUARIO_STORAGE_KEY);
  if (!crudo) return null;
  try {
    return JSON.parse(crudo);
  } catch {
    return null;
  }
}

function guardarSesion(token, usuario) {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
  localStorage.setItem(USUARIO_STORAGE_KEY, JSON.stringify(usuario));
}

function limpiarSesion() {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(USUARIO_STORAGE_KEY);
}

/**
 * Ejecuta una petición HTTP contra el backend y homogeniza los errores:
 * - Si el servidor no responde (backend apagado, CORS, sin red) lanza un
 *   ApiError con status 0 y un mensaje entendible por el usuario final.
 * - Si el backend responde con un error (4xx/5xx), usa el "mensaje" que
 *   entrega el GlobalExceptionHandler (o el primer detalle de validación).
 * - Si la respuesta no tiene cuerpo (204 No Content), devuelve null.
 */
async function peticionApi(ruta, opciones = {}) {
  const { method = 'GET', body, conAuth = true } = opciones;

  const headers = { 'Content-Type': 'application/json' };
  if (conAuth) {
    const token = obtenerToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  let respuesta;
  try {
    respuesta = await fetch(`${API_BASE_URL}${ruta}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (error) {
    throw new ApiError(
      'No se pudo conectar con el servidor de BuildZone. Verifica que el backend esté encendido.',
      0,
      null
    );
  }

  if (respuesta.status === 204) {
    return null;
  }

  const texto = await respuesta.text();
  let datos = null;
  if (texto) {
    try {
      datos = JSON.parse(texto);
    } catch {
      datos = null;
    }
  }

  if (!respuesta.ok) {
    let mensaje = `Ocurrió un error (${respuesta.status})`;
    if (datos) {
      if (Array.isArray(datos.detalles) && datos.detalles.length > 0) {
        mensaje = datos.detalles.join(' ');
      } else if (datos.mensaje) {
        mensaje = datos.mensaje;
      }
    }
    if (respuesta.status === 401) {
      // El token expiró o es inválido: se limpia la sesión local para que
      // la interfaz vuelva al estado "no autenticado" de forma consistente.
      limpiarSesion();
    }
    throw new ApiError(mensaje, respuesta.status, datos);
  }

  return datos;
}

// ── Autenticación (/api/auth) ──

function registrarUsuario({ nombre, apellido, username, email, password }) {
  return peticionApi('/auth/registro', {
    method: 'POST',
    conAuth: false,
    body: { nombre, apellido, username, email, password },
  });
}

function iniciarSesion({ identificador, password }) {
  return peticionApi('/auth/login', {
    method: 'POST',
    conAuth: false,
    body: { identificador, password },
  });
}

// ── Mi perfil (/api/usuarios/me) ──

function obtenerMiPerfil() {
  return peticionApi('/usuarios/me');
}

function actualizarMiPerfil({ nombre, apellido, email }) {
  return peticionApi('/usuarios/me', {
    method: 'PUT',
    body: { nombre, apellido, email },
  });
}

function cambiarMiPassword({ passwordActual, passwordNueva }) {
  return peticionApi('/usuarios/me/password', {
    method: 'PUT',
    body: { passwordActual, passwordNueva },
  });
}

// ── Administración de usuarios (/api/usuarios) — solo ADMIN ──

function listarUsuarios() {
  return peticionApi('/usuarios');
}

function cambiarRolUsuario(id, rol) {
  return peticionApi(`/usuarios/${id}/rol`, { method: 'PATCH', body: { rol } });
}

function cambiarEstadoUsuario(id, estado) {
  return peticionApi(`/usuarios/${id}/estado`, { method: 'PATCH', body: { estado } });
}

// ── Planes de suscripción (/api/planes) ──

function listarPlanesActivos() {
  return peticionApi('/planes', { conAuth: false });
}

function listarTodosLosPlanes() {
  return peticionApi('/planes/todos');
}

function crearPlan({ nombre, descripcion, precio, duracionDias }) {
  return peticionApi('/planes', {
    method: 'POST',
    body: { nombre, descripcion, precio, duracionDias },
  });
}

function actualizarPlan(id, { nombre, descripcion, precio, duracionDias }) {
  return peticionApi(`/planes/${id}`, {
    method: 'PUT',
    body: { nombre, descripcion, precio, duracionDias },
  });
}

function desactivarPlan(id) {
  return peticionApi(`/planes/${id}`, { method: 'DELETE' });
}

// ── Suscripciones (/api/suscripciones) ──

function suscribirseAPlan(planId) {
  return peticionApi('/suscripciones', { method: 'POST', body: { planId } });
}

function obtenerMiSuscripcionActiva() {
  return peticionApi('/suscripciones/me/activa');
}

function obtenerMiHistorialSuscripciones() {
  return peticionApi('/suscripciones/me');
}

function cancelarSuscripcion(id) {
  return peticionApi(`/suscripciones/${id}`, { method: 'DELETE' });
}

function listarTodasLasSuscripciones() {
  return peticionApi('/suscripciones');
}

// ── Catálogo (/api/productos, /api/marcas, /api/categorias) ──
// Las consultas (GET) son públicas; crear/editar/eliminar exige rol ADMIN.

function listarProductos() {
  return peticionApi('/productos', { conAuth: false });
}

function crearProducto(datos) {
  return peticionApi('/productos', { method: 'POST', body: datos });
}

function actualizarProducto(id, datos) {
  return peticionApi(`/productos/${id}`, { method: 'PUT', body: datos });
}

function eliminarProducto(id) {
  return peticionApi(`/productos/${id}`, { method: 'DELETE' });
}

function listarMarcas() {
  return peticionApi('/marcas', { conAuth: false });
}

function crearMarca({ nombre, descripcion }) {
  return peticionApi('/marcas', { method: 'POST', body: { nombre, descripcion } });
}

function listarCategorias() {
  return peticionApi('/categorias', { conAuth: false });
}

function crearCategoria({ nombre, descripcion }) {
  return peticionApi('/categorias', { method: 'POST', body: { nombre, descripcion } });
}

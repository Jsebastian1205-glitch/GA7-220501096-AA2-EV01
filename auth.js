/**
 * Sesión de usuario: login, registro, logout y actualización de la barra
 * de navegación según el estado de autenticación (invitado vs. usuario vs.
 * administrador).
 *
 * Depende de ui.js (mostrarExito/mostrarError) y de api.js (iniciarSesion,
 * registrarUsuario, obtenerMiPerfil, obtenerToken/guardarSesion/etc.).
 * Se inicializa desde script.js llamando a window.inicializarAuth() dentro
 * de su propio DOMContentLoaded, así que ese orden de carga es obligatorio.
 */

// Estado en memoria de la sesión actual; se siembra desde localStorage
// (ver api.js) para que la sesión sobreviva a un refresh de la página.
let usuarioActual = obtenerUsuarioGuardado();

function estaAutenticado() {
  return !!usuarioActual;
}

function esAdmin() {
  return !!usuarioActual && usuarioActual.rol === 'ADMIN';
}

/** Actualiza el icono de login / el menú de usuario y el enlace "Admin". */
function actualizarNavSegunSesion() {
  const loginBtn = document.getElementById('nav-login-btn');
  const userArea = document.getElementById('nav-user-area');
  const navAdmin = document.getElementById('nav-admin');

  if (!loginBtn || !userArea) return;

  if (!usuarioActual) {
    loginBtn.style.display = '';
    userArea.style.display = 'none';
    userArea.innerHTML = '';
    if (navAdmin) navAdmin.style.display = 'none';
    return;
  }

  loginBtn.style.display = 'none';
  userArea.style.display = '';
  if (navAdmin) navAdmin.style.display = esAdmin() ? '' : 'none';

  const inicial = (usuarioActual.username || usuarioActual.nombre || '?').charAt(0).toUpperCase();
  userArea.innerHTML = `
    <div class="user-menu">
      <button class="user-menu-btn" id="user-menu-btn" type="button">
        <span class="user-avatar">${inicial}</span>
        <span class="user-name">${usuarioActual.username}</span>
        <span class="user-caret">▾</span>
      </button>
      <div class="user-dropdown" id="user-dropdown">
        <div class="user-dropdown-header">
          <div class="user-dropdown-name">${usuarioActual.nombre} ${usuarioActual.apellido}</div>
          <div class="user-dropdown-email">${usuarioActual.email}</div>
        </div>
        <div class="user-dropdown-item" data-action="cuenta">👤 Mi cuenta</div>
        ${esAdmin() ? '<div class="user-dropdown-item" data-action="admin">🛠️ Panel admin</div>' : ''}
        <div class="user-dropdown-divider"></div>
        <div class="user-dropdown-item user-dropdown-item-danger" data-action="logout">🚪 Cerrar sesión</div>
      </div>
    </div>`;
}

function abrirDropdownUsuario(abrir) {
  const dropdown = document.getElementById('user-dropdown');
  if (dropdown) dropdown.classList.toggle('user-dropdown-open', abrir);
}

async function manejarEnvioLogin(event) {
  event.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;
  const boton = event.target.querySelector('button[type="submit"]');

  try {
    if (boton) boton.disabled = true;
    const respuesta = await iniciarSesion({ identificador: email, password });
    guardarSesion(respuesta.token, respuesta.usuario);
    usuarioActual = respuesta.usuario;
    actualizarNavSegunSesion();
    toggleLogin(false);
    event.target.reset();
    mostrarExito(`¡Bienvenido de nuevo, ${respuesta.usuario.nombre}!`);
  } catch (error) {
    mostrarError(error.message);
  } finally {
    if (boton) boton.disabled = false;
  }
}

async function manejarEnvioRegistro(event) {
  event.preventDefault();
  const nombreCompleto = document.getElementById('register-name').value.trim();
  const email = document.getElementById('register-email').value.trim();
  const password = document.getElementById('register-password').value;
  const passwordConfirm = document.getElementById('register-password-confirm').value;
  const boton = event.target.querySelector('button[type="submit"]');

  if (password !== passwordConfirm) {
    mostrarError('Las contraseñas no coinciden.');
    return;
  }

  // El formulario pide "Nombre completo" en un solo campo (así está
  // diseñado en el HTML original), pero el backend espera nombre, apellido
  // y username por separado: se deriva un reparto razonable a partir de lo
  // escrito, y un username disponible a partir del correo.
  const partes = nombreCompleto.split(/\s+/).filter(Boolean);
  const nombre = partes[0] || nombreCompleto;
  const apellido = partes.slice(1).join(' ') || partes[0] || nombreCompleto;
  const username = email.split('@')[0].toLowerCase().replace(/[^a-z0-9._-]/g, '') || `usuario${Date.now()}`;

  try {
    if (boton) boton.disabled = true;
    const respuesta = await registrarUsuario({ nombre, apellido, username, email, password });
    guardarSesion(respuesta.token, respuesta.usuario);
    usuarioActual = respuesta.usuario;
    actualizarNavSegunSesion();
    toggleRegister(false);
    event.target.reset();
    mostrarExito(`¡Cuenta creada! Bienvenido a BuildZone, ${respuesta.usuario.nombre}.`);
  } catch (error) {
    mostrarError(error.message);
  } finally {
    if (boton) boton.disabled = false;
  }
}

function cerrarSesion() {
  limpiarSesion();
  usuarioActual = null;
  actualizarNavSegunSesion();
  mostrarExito('Sesión cerrada correctamente.');
  showPage('inicio');
}

/**
 * Vuelve a pedir el perfil al backend para confirmar que el token guardado
 * sigue siendo válido y refrescar los datos (por ejemplo si el rol cambió
 * desde el panel de administración). Si el token expiró, peticionApi ya se
 * encarga de limpiar la sesión local; aquí solo reflejamos ese resultado en
 * la interfaz.
 */
async function revalidarSesion() {
  if (!usuarioActual) return;
  try {
    const perfil = await obtenerMiPerfil();
    usuarioActual = perfil;
    guardarSesion(obtenerToken(), perfil);
  } catch {
    usuarioActual = obtenerUsuarioGuardado();
  } finally {
    actualizarNavSegunSesion();
  }
}

function inicializarAuth() {
  actualizarNavSegunSesion();
  revalidarSesion();

  const loginForm = document.getElementById('login-form');
  if (loginForm) loginForm.addEventListener('submit', manejarEnvioLogin);

  const registerForm = document.getElementById('register-form');
  if (registerForm) registerForm.addEventListener('submit', manejarEnvioRegistro);

  const userArea = document.getElementById('nav-user-area');
  if (userArea) {
    userArea.addEventListener('click', (event) => {
      const boton = event.target.closest('#user-menu-btn');
      if (boton) {
        const dropdown = document.getElementById('user-dropdown');
        const abierto = dropdown?.classList.contains('user-dropdown-open');
        abrirDropdownUsuario(!abierto);
        return;
      }

      const item = event.target.closest('[data-action]');
      if (!item) return;
      abrirDropdownUsuario(false);

      if (item.dataset.action === 'cuenta') {
        showPage('cuenta');
        window.cargarCuenta?.();
      } else if (item.dataset.action === 'admin') {
        showPage('admin');
        window.cargarAdmin?.();
      } else if (item.dataset.action === 'logout') {
        cerrarSesion();
      }
    });
  }

  // Cierra el menú de usuario al hacer clic en cualquier otro lugar.
  document.addEventListener('click', (event) => {
    const userArea = document.getElementById('nav-user-area');
    if (userArea && !userArea.contains(event.target)) {
      abrirDropdownUsuario(false);
    }
  });
}

window.inicializarAuth = inicializarAuth;
window.estaAutenticado = estaAutenticado;
window.esAdmin = esAdmin;
window.cerrarSesion = cerrarSesion;
window.obtenerUsuarioActual = () => usuarioActual;

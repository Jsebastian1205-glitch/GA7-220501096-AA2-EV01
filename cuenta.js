/**
 * Página "Mi cuenta": edición de perfil, cambio de contraseña e historial /
 * cancelación de la suscripción propia. Depende de ui.js, api.js y de
 * auth.js (para conocer/actualizar al usuario autenticado).
 */

function cambiarTabCuenta(nombreTab) {
  document.querySelectorAll('#page-cuenta .account-tab').forEach((tab) => {
    tab.classList.toggle('active', tab.dataset.tab === nombreTab);
  });
  document.querySelectorAll('#page-cuenta .account-panel').forEach((panel) => {
    panel.classList.toggle('active', panel.id === `account-panel-${nombreTab}`);
  });
}

/** Rellena el formulario de perfil con los datos del usuario autenticado. */
function cargarFormularioPerfil() {
  const usuario = window.obtenerUsuarioActual ? window.obtenerUsuarioActual() : null;
  if (!usuario) return;

  const username = document.getElementById('perfil-username');
  const nombre = document.getElementById('perfil-nombre');
  const apellido = document.getElementById('perfil-apellido');
  const email = document.getElementById('perfil-email');

  if (username) username.value = usuario.username;
  if (nombre) nombre.value = usuario.nombre;
  if (apellido) apellido.value = usuario.apellido;
  if (email) email.value = usuario.email;
}

function formatearFecha(fechaIso) {
  if (!fechaIso) return '—';
  const [anio, mes, dia] = fechaIso.split('-');
  return `${dia}/${mes}/${anio}`;
}

const ETIQUETAS_ESTADO_SUSCRIPCION = {
  ACTIVA: { texto: 'Activa', clase: 'badge-verde' },
  CANCELADA: { texto: 'Cancelada', clase: 'badge-gris' },
  VENCIDA: { texto: 'Vencida', clase: 'badge-rojo' },
};

function renderizarSuscripcionActual(suscripcion) {
  const contenedor = document.getElementById('suscripcion-actual');
  if (!contenedor) return;

  if (!suscripcion) {
    contenedor.innerHTML = `
      <div class="plans-empty">
        Todavía no tienes una suscripción activa.
        <span class="form-link" onclick="showPage('planes'); window.cargarPlanes?.()">Ver planes disponibles</span>
      </div>`;
    return;
  }

  contenedor.innerHTML = `
    <div class="subscription-card">
      <div class="subscription-card-info">
        <div class="subscription-card-plan">${suscripcion.plan.nombre}</div>
        <div class="subscription-card-fechas">Desde ${formatearFecha(suscripcion.fechaInicio)} hasta ${formatearFecha(suscripcion.fechaFin)}</div>
      </div>
      <button class="btn btn-outline" id="btn-cancelar-suscripcion" data-suscripcion-id="${suscripcion.id}">Cancelar suscripción</button>
    </div>`;
}

function renderizarHistorialSuscripciones(historial) {
  const contenedor = document.getElementById('suscripcion-historial');
  if (!contenedor) return;

  if (!historial || historial.length === 0) {
    contenedor.innerHTML = '<div class="plans-empty">Aún no tienes historial de suscripciones.</div>';
    return;
  }

  contenedor.innerHTML = `
    <div class="admin-table-wrap">
      <table class="admin-table">
        <thead><tr><th>Plan</th><th>Inicio</th><th>Fin</th><th>Estado</th></tr></thead>
        <tbody>
          ${historial
            .map((s) => {
              const estado = ETIQUETAS_ESTADO_SUSCRIPCION[s.estado] || { texto: s.estado, clase: 'badge-gris' };
              return `
                <tr>
                  <td>${s.plan.nombre}</td>
                  <td>${formatearFecha(s.fechaInicio)}</td>
                  <td>${formatearFecha(s.fechaFin)}</td>
                  <td><span class="badge ${estado.clase}">${estado.texto}</span></td>
                </tr>`;
            })
            .join('')}
        </tbody>
      </table>
    </div>`;
}

async function cargarCuenta() {
  cargarFormularioPerfil();

  const actualEl = document.getElementById('suscripcion-actual');
  const historialEl = document.getElementById('suscripcion-historial');
  if (actualEl) actualEl.innerHTML = '<div class="plans-empty">Cargando tu suscripción…</div>';
  if (historialEl) historialEl.innerHTML = '';

  try {
    const [suscripcionActiva, historial] = await Promise.all([
      obtenerMiSuscripcionActiva(),
      obtenerMiHistorialSuscripciones(),
    ]);
    renderizarSuscripcionActual(suscripcionActiva);
    renderizarHistorialSuscripciones(historial);
  } catch (error) {
    if (actualEl) actualEl.innerHTML = `<div class="plans-empty">No se pudo cargar tu suscripción: ${error.message}</div>`;
  }
}

async function manejarEnvioPerfil(event) {
  event.preventDefault();
  const nombre = document.getElementById('perfil-nombre').value.trim();
  const apellido = document.getElementById('perfil-apellido').value.trim();
  const email = document.getElementById('perfil-email').value.trim();
  const boton = event.target.querySelector('button[type="submit"]');

  try {
    if (boton) boton.disabled = true;
    const actualizado = await actualizarMiPerfil({ nombre, apellido, email });
    guardarSesion(obtenerToken(), actualizado);
    usuarioActual = actualizado;
    actualizarNavSegunSesion();
    mostrarExito('Perfil actualizado correctamente.');
  } catch (error) {
    mostrarError(error.message);
  } finally {
    if (boton) boton.disabled = false;
  }
}

async function manejarEnvioPassword(event) {
  event.preventDefault();
  const passwordActual = document.getElementById('password-actual').value;
  const passwordNueva = document.getElementById('password-nueva').value;
  const boton = event.target.querySelector('button[type="submit"]');

  try {
    if (boton) boton.disabled = true;
    await cambiarMiPassword({ passwordActual, passwordNueva });
    mostrarExito('Contraseña actualizada correctamente.');
    event.target.reset();
  } catch (error) {
    mostrarError(error.message);
  } finally {
    if (boton) boton.disabled = false;
  }
}

async function manejarClicCancelarSuscripcion(event) {
  const boton = event.target.closest('#btn-cancelar-suscripcion');
  if (!boton) return;

  boton.disabled = true;
  try {
    await cancelarSuscripcion(Number(boton.dataset.suscripcionId));
    mostrarExito('Suscripción cancelada.');
    await cargarCuenta();
  } catch (error) {
    mostrarError(error.message);
    boton.disabled = false;
  }
}

function inicializarCuenta() {
  document.querySelectorAll('#page-cuenta .account-tab').forEach((tab) => {
    tab.addEventListener('click', () => cambiarTabCuenta(tab.dataset.tab));
  });

  const perfilForm = document.getElementById('perfil-form');
  if (perfilForm) perfilForm.addEventListener('submit', manejarEnvioPerfil);

  const passwordForm = document.getElementById('password-form');
  if (passwordForm) passwordForm.addEventListener('submit', manejarEnvioPassword);

  const suscripcionActualEl = document.getElementById('suscripcion-actual');
  if (suscripcionActualEl) suscripcionActualEl.addEventListener('click', manejarClicCancelarSuscripcion);
}

window.inicializarCuenta = inicializarCuenta;
window.cargarCuenta = cargarCuenta;

/**
 * Panel de administración: gestión de usuarios (rol/estado), CRUD de
 * planes de suscripción y visualización de todas las suscripciones.
 * Todos los endpoints que usa este módulo exigen rol ADMIN en el backend;
 * el acceso a la página ya está bloqueado en script.js (showPage) para
 * quien no sea admin, así que aquí se asume que sí lo es.
 */

let usuariosCache = [];
let planesAdminCache = [];

function cambiarTabAdmin(nombreTab) {
  document.querySelectorAll('#page-admin .account-tab').forEach((tab) => {
    tab.classList.toggle('active', tab.dataset.adminTab === nombreTab);
  });
  document.querySelectorAll('#page-admin .account-panel').forEach((panel) => {
    panel.classList.toggle('active', panel.id === `admin-panel-${nombreTab}`);
  });
}

// ── Usuarios ──

const ETIQUETAS_ESTADO_USUARIO = {
  ACTIVO: 'badge-verde',
  INACTIVO: 'badge-rojo',
};

function renderizarUsuarios(usuarios) {
  const tbody = document.querySelector('#admin-usuarios-table tbody');
  if (!tbody) return;

  if (!usuarios || usuarios.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5">No hay usuarios registrados.</td></tr>';
    return;
  }

  tbody.innerHTML = usuarios
    .map((u) => {
      const nuevoRol = u.rol === 'ADMIN' ? 'USUARIO' : 'ADMIN';
      const nuevoEstado = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
      return `
        <tr>
          <td>${u.username}<br><span class="table-subtext">${u.nombre} ${u.apellido}</span></td>
          <td>${u.email}</td>
          <td><span class="badge ${u.rol === 'ADMIN' ? 'badge-purple' : 'badge-gris'}">${u.rol}</span></td>
          <td><span class="badge ${ETIQUETAS_ESTADO_USUARIO[u.estado] || 'badge-gris'}">${u.estado}</span></td>
          <td class="admin-table-actions">
            <button class="btn-mini" data-rol-usuario-id="${u.id}" data-nuevo-rol="${nuevoRol}">Hacer ${nuevoRol}</button>
            <button class="btn-mini ${u.estado === 'ACTIVO' ? 'btn-mini-danger' : ''}" data-estado-usuario-id="${u.id}" data-nuevo-estado="${nuevoEstado}">${u.estado === 'ACTIVO' ? 'Desactivar' : 'Activar'}</button>
          </td>
        </tr>`;
    })
    .join('');
}

async function manejarClicUsuarios(event) {
  const botonRol = event.target.closest('[data-rol-usuario-id]');
  const botonEstado = event.target.closest('[data-estado-usuario-id]');

  try {
    if (botonRol) {
      botonRol.disabled = true;
      await cambiarRolUsuario(Number(botonRol.dataset.rolUsuarioId), botonRol.dataset.nuevoRol);
      mostrarExito('Rol actualizado.');
      await cargarUsuarios();
    } else if (botonEstado) {
      botonEstado.disabled = true;
      await cambiarEstadoUsuario(Number(botonEstado.dataset.estadoUsuarioId), botonEstado.dataset.nuevoEstado);
      mostrarExito('Estado actualizado.');
      await cargarUsuarios();
    }
  } catch (error) {
    mostrarError(error.message);
  }
}

async function cargarUsuarios() {
  const tbody = document.querySelector('#admin-usuarios-table tbody');
  if (tbody) tbody.innerHTML = '<tr><td colspan="5">Cargando…</td></tr>';
  try {
    usuariosCache = await listarUsuarios();
    renderizarUsuarios(usuariosCache);
  } catch (error) {
    if (tbody) tbody.innerHTML = `<tr><td colspan="5">Error al cargar usuarios: ${error.message}</td></tr>`;
  }
}

// ── Planes (CRUD) ──

function renderizarPlanesAdmin(planes) {
  const tbody = document.querySelector('#admin-planes-table tbody');
  if (!tbody) return;

  if (!planes || planes.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5">No hay planes creados todavía.</td></tr>';
    return;
  }

  tbody.innerHTML = planes
    .map(
      (p) => `
        <tr>
          <td>${p.nombre}</td>
          <td>${formatearMoneda(p.precio)}</td>
          <td>${p.duracionDias} días</td>
          <td><span class="badge ${p.activo ? 'badge-verde' : 'badge-rojo'}">${p.activo ? 'Activo' : 'Inactivo'}</span></td>
          <td class="admin-table-actions">
            <button class="btn-mini" data-editar-plan-id="${p.id}">Editar</button>
            ${p.activo ? `<button class="btn-mini btn-mini-danger" data-desactivar-plan-id="${p.id}">Desactivar</button>` : ''}
          </td>
        </tr>`
    )
    .join('');
}

async function cargarPlanesAdmin() {
  const tbody = document.querySelector('#admin-planes-table tbody');
  if (tbody) tbody.innerHTML = '<tr><td colspan="5">Cargando…</td></tr>';
  try {
    planesAdminCache = await listarTodosLosPlanes();
    renderizarPlanesAdmin(planesAdminCache);
  } catch (error) {
    if (tbody) tbody.innerHTML = `<tr><td colspan="5">Error al cargar planes: ${error.message}</td></tr>`;
  }
}

function abrirModalPlan(plan) {
  const modal = document.getElementById('plan-modal');
  const titulo = document.getElementById('plan-modal-title');
  if (!modal) return;

  document.getElementById('plan-id').value = plan ? plan.id : '';
  document.getElementById('plan-nombre').value = plan ? plan.nombre : '';
  document.getElementById('plan-descripcion').value = plan ? plan.descripcion || '' : '';
  document.getElementById('plan-precio').value = plan ? plan.precio : '';
  document.getElementById('plan-duracion').value = plan ? plan.duracionDias : '';
  if (titulo) titulo.textContent = plan ? 'Editar plan' : 'Nuevo plan';

  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

function cerrarModalPlan() {
  const modal = document.getElementById('plan-modal');
  if (!modal) return;
  modal.classList.remove('active');
  document.body.style.overflow = '';
}

async function manejarClicPlanesAdmin(event) {
  const botonEditar = event.target.closest('[data-editar-plan-id]');
  const botonDesactivar = event.target.closest('[data-desactivar-plan-id]');
  const botonNuevo = event.target.closest('#btn-nuevo-plan');

  if (botonNuevo) {
    abrirModalPlan(null);
    return;
  }

  if (botonEditar) {
    const plan = planesAdminCache.find((p) => p.id === Number(botonEditar.dataset.editarPlanId));
    abrirModalPlan(plan || null);
    return;
  }

  if (botonDesactivar) {
    if (!confirm('¿Desactivar este plan? Los usuarios ya no podrán suscribirse a él.')) return;
    botonDesactivar.disabled = true;
    try {
      await desactivarPlan(Number(botonDesactivar.dataset.desactivarPlanId));
      mostrarExito('Plan desactivado.');
      await cargarPlanesAdmin();
    } catch (error) {
      mostrarError(error.message);
      botonDesactivar.disabled = false;
    }
  }
}

async function manejarEnvioPlan(event) {
  event.preventDefault();
  const id = document.getElementById('plan-id').value;
  const datos = {
    nombre: document.getElementById('plan-nombre').value.trim(),
    descripcion: document.getElementById('plan-descripcion').value.trim(),
    precio: Number(document.getElementById('plan-precio').value),
    duracionDias: Number(document.getElementById('plan-duracion').value),
  };
  const boton = document.getElementById('plan-form-submit');

  try {
    if (boton) boton.disabled = true;
    if (id) {
      await actualizarPlan(Number(id), datos);
      mostrarExito('Plan actualizado.');
    } else {
      await crearPlan(datos);
      mostrarExito('Plan creado.');
    }
    cerrarModalPlan();
    await cargarPlanesAdmin();
  } catch (error) {
    mostrarError(error.message);
  } finally {
    if (boton) boton.disabled = false;
  }
}

// ── Suscripciones (solo lectura) ──

const ETIQUETAS_ESTADO_SUSCRIPCION_ADMIN = {
  ACTIVA: 'badge-verde',
  CANCELADA: 'badge-gris',
  VENCIDA: 'badge-rojo',
};

function renderizarSuscripcionesAdmin(suscripciones) {
  const tbody = document.querySelector('#admin-suscripciones-table tbody');
  if (!tbody) return;

  if (!suscripciones || suscripciones.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5">No hay suscripciones registradas.</td></tr>';
    return;
  }

  const usuariosPorId = new Map(usuariosCache.map((u) => [u.id, u]));

  tbody.innerHTML = suscripciones
    .map((s) => {
      const usuario = usuariosPorId.get(s.usuarioId);
      const badge = ETIQUETAS_ESTADO_SUSCRIPCION_ADMIN[s.estado] || 'badge-gris';
      return `
        <tr>
          <td>${usuario ? usuario.username : `#${s.usuarioId}`}</td>
          <td>${s.plan.nombre}</td>
          <td>${s.fechaInicio}</td>
          <td>${s.fechaFin}</td>
          <td><span class="badge ${badge}">${s.estado}</span></td>
        </tr>`;
    })
    .join('');
}

async function cargarSuscripcionesAdmin() {
  const tbody = document.querySelector('#admin-suscripciones-table tbody');
  if (tbody) tbody.innerHTML = '<tr><td colspan="5">Cargando…</td></tr>';
  try {
    const suscripciones = await listarTodasLasSuscripciones();
    renderizarSuscripcionesAdmin(suscripciones);
  } catch (error) {
    if (tbody) tbody.innerHTML = `<tr><td colspan="5">Error al cargar suscripciones: ${error.message}</td></tr>`;
  }
}

// ── Inicialización ──

async function cargarAdmin() {
  await cargarUsuarios();
  await Promise.all([cargarPlanesAdmin(), cargarSuscripcionesAdmin()]);
}

function inicializarAdmin() {
  document.querySelectorAll('#page-admin .account-tab').forEach((tab) => {
    tab.addEventListener('click', () => cambiarTabAdmin(tab.dataset.adminTab));
  });

  const tablaUsuarios = document.getElementById('admin-usuarios-table');
  if (tablaUsuarios) tablaUsuarios.addEventListener('click', manejarClicUsuarios);

  const panelPlanes = document.getElementById('admin-panel-planes');
  if (panelPlanes) panelPlanes.addEventListener('click', manejarClicPlanesAdmin);

  const planForm = document.getElementById('plan-form');
  if (planForm) planForm.addEventListener('submit', manejarEnvioPlan);

  const planModal = document.getElementById('plan-modal');
  if (planModal) {
    planModal.addEventListener('click', (event) => {
      if (event.target === planModal || event.target.closest('[data-close-plan-modal]')) {
        cerrarModalPlan();
      }
    });
  }
}

window.inicializarAdmin = inicializarAdmin;
window.cargarAdmin = cargarAdmin;

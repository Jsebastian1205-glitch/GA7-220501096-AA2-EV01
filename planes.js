/**
 * Página pública de Planes: lista los planes activos (GET /api/planes, sin
 * sesión) y permite suscribirse (requiere sesión). Depende de ui.js
 * (toasts, formatearMoneda), api.js (listarPlanesActivos, suscribirseAPlan,
 * obtenerMiSuscripcionActiva) y auth.js (estaAutenticado, toggleLogin).
 */

async function cargarPlanes() {
  const grid = document.getElementById('plans-grid');
  if (!grid) return;

  grid.innerHTML = '<div class="plans-empty">Cargando planes…</div>';

  let planes;
  try {
    planes = await listarPlanesActivos();
  } catch (error) {
    grid.innerHTML = `<div class="plans-empty">No se pudieron cargar los planes: ${error.message}</div>`;
    return;
  }

  let suscripcionActiva = null;
  if (typeof estaAutenticado === 'function' && estaAutenticado()) {
    try {
      suscripcionActiva = await obtenerMiSuscripcionActiva();
    } catch {
      suscripcionActiva = null;
    }
  }

  renderizarPlanes(planes, suscripcionActiva);
}

function renderizarPlanes(planes, suscripcionActiva) {
  const grid = document.getElementById('plans-grid');
  if (!grid) return;

  if (!planes || planes.length === 0) {
    grid.innerHTML = '<div class="plans-empty">Todavía no hay planes disponibles.</div>';
    return;
  }

  grid.innerHTML = planes
    .map((plan) => {
      const esActual = !!(suscripcionActiva && suscripcionActiva.plan && suscripcionActiva.plan.id === plan.id);
      return `
        <div class="plan-card ${esActual ? 'plan-card-current' : ''}">
          ${esActual ? '<div class="plan-card-badge">⭐ Tu plan actual</div>' : ''}
          <div class="plan-card-name">${plan.nombre}</div>
          <div class="plan-card-price">${formatearMoneda(plan.precio)}<span class="plan-card-period">/${plan.duracionDias} días</span></div>
          <div class="plan-card-desc">${plan.descripcion || ''}</div>
          <button
            class="btn ${esActual ? 'btn-outline' : 'btn-primary'} btn-full"
            data-plan-id="${plan.id}"
            ${esActual ? 'disabled' : ''}
          >${esActual ? 'Ya estás suscrito' : 'Suscribirme'}</button>
        </div>`;
    })
    .join('');
}

async function manejarClicPlan(event) {
  const boton = event.target.closest('[data-plan-id]');
  if (!boton || boton.disabled) return;

  if (!(typeof estaAutenticado === 'function' && estaAutenticado())) {
    mostrarError('Inicia sesión para suscribirte a un plan.');
    toggleLogin(true);
    return;
  }

  const planId = Number(boton.dataset.planId);
  boton.disabled = true;
  const textoOriginal = boton.textContent;
  boton.textContent = 'Procesando…';

  try {
    await suscribirseAPlan(planId);
    mostrarExito('¡Suscripción realizada con éxito!');
    await cargarPlanes();
  } catch (error) {
    mostrarError(error.message);
    boton.disabled = false;
    boton.textContent = textoOriginal;
  }
}

function inicializarPlanes() {
  const grid = document.getElementById('plans-grid');
  if (grid) grid.addEventListener('click', manejarClicPlan);
  cargarPlanes();
}

window.inicializarPlanes = inicializarPlanes;
window.cargarPlanes = cargarPlanes;

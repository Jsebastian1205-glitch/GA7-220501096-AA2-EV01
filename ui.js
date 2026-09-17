/**
 * Utilidades de interfaz compartidas por el resto de los módulos:
 * notificaciones tipo "toast" y formateo de moneda/fecha.
 *
 * Se aisla en su propio archivo (sin dependencias) para que cualquier
 * módulo (auth.js, planes.js, cuenta.js, admin.js, script.js) pueda
 * usarlo sin preocuparse del orden de carga, siempre que este archivo
 * se incluya primero en Index.html.
 */

const FORMATEADOR_MONEDA = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

/** Formatea un número como precio en pesos colombianos, p. ej. $29.900. */
function formatearMoneda(valor) {
  const numero = Number(valor);
  return Number.isFinite(numero) ? FORMATEADOR_MONEDA.format(numero) : '$0';
}

/**
 * Muestra una notificación flotante ("toast") en la esquina inferior
 * derecha. tipo: 'exito' | 'error' | 'info' (por defecto 'info').
 */
function mostrarToast(mensaje, tipo) {
  let contenedor = document.getElementById('toast-container');
  if (!contenedor) {
    contenedor = document.createElement('div');
    contenedor.id = 'toast-container';
    document.body.appendChild(contenedor);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${tipo || 'info'}`;
  toast.textContent = mensaje;
  contenedor.appendChild(toast);

  // Fuerza el reflow para que la animación de entrada se reproduzca.
  requestAnimationFrame(() => toast.classList.add('toast-visible'));

  setTimeout(() => {
    toast.classList.remove('toast-visible');
    setTimeout(() => toast.remove(), 300);
  }, 4200);
}

function mostrarExito(mensaje) {
  mostrarToast(mensaje, 'exito');
}

function mostrarError(mensaje) {
  mostrarToast(mensaje, 'error');
}

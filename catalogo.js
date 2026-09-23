/**
 * Módulo de catálogo: reemplaza los productos de prueba de script.js por
 * los productos reales de la API (GET /api/productos) y ofrece la pestaña
 * "Catálogo" del panel de administración (CRUD de productos y alta rápida
 * de marcas/categorías).
 *
 * Patrón de degradación elegante: si el backend no responde, la página
 * sigue funcionando con los datos locales de script.js.
 *
 * Depende de ui.js (escaparHtml, toasts), api.js (listarProductos, ...) y
 * de las variables/funciones globales de script.js (PRODUCTS,
 * renderProductsGrid, populateDuelSelects). Se inicializa desde script.js.
 */

// ── Adaptación de datos API -> formato del front-end ──

/** Iconos por categoría (los mismos de las tarjetas de categoría). */
const ICONOS_CATEGORIA = {
  cpu: '🕹️', gpu: '🎮', ram: '💾', almacenamiento: '💿', 'placas-madre': '🔌',
  refrigeracion: '❄️', accesorios: '🖱️', fuentes: '🔋', monitores: '🖥️',
};

/** Reglas (palabra clave -> slug) para ubicar cada categoría de la BD en un filtro. */
const REGLAS_CATEGORIA = [
  [/procesador|cpu/, 'cpu'],
  [/grafica|gpu|video/, 'gpu'],
  [/memoria|ram/, 'ram'],
  [/almacenamiento|ssd|disco|nvme/, 'almacenamiento'],
  [/placa|board/, 'placas-madre'],
  [/refrigeracion|cooler|disipador/, 'refrigeracion'],
  [/fuente|psu/, 'fuentes'],
  [/monitor|pantalla/, 'monitores'],
];

// Copia de los productos locales de script.js: se usa como fuente de
// precio y estadísticas de referencia, que todavía no existen en la BD.
let productosReferencia = null;
let catalogoAdminCache = [];

/** Minúsculas y sin tildes, para comparar textos de forma tolerante. */
function normalizarTexto(texto) {
  return String(texto || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .trim();
}

function slugCategoria(nombreCategoria) {
  const nombre = normalizarTexto(nombreCategoria);
  const regla = REGLAS_CATEGORIA.find(([patron]) => patron.test(nombre));
  return regla ? regla[1] : 'accesorios';
}

function buscarReferencia(marca, nombre) {
  const clave = `${normalizarTexto(marca)} ${normalizarTexto(nombre)}`;
  return (productosReferencia || []).find(
    (p) => `${normalizarTexto(p.brand)} ${normalizarTexto(p.name)}` === clave
  );
}

/** Convierte un ProductoResponse del backend al formato que usa script.js. */
function adaptarProducto(dto) {
  const category = slugCategoria(dto.nombreCategoria);
  const referencia = buscarReferencia(dto.nombreMarca, dto.nombre);
  return {
    id: `api-${dto.idProducto}`,
    idProducto: dto.idProducto,
    category,
    categoryName: dto.nombreCategoria,
    brand: dto.nombreMarca,
    name: dto.nombre,
    description: dto.descripcion || '',
    price: referencia ? referencia.price : null,
    icon: ICONOS_CATEGORIA[category] || '🧩',
    stats: referencia ? referencia.stats : null,
  };
}

/**
 * Descarga el catálogo real y actualiza la página de Productos y el
 * Comparador. Devuelve true si se usaron datos de la API.
 */
async function cargarCatalogoDesdeApi() {
  if (productosReferencia === null) productosReferencia = PRODUCTS.slice();

  try {
    const productos = await listarProductos();
    // PRODUCTS es const en script.js: se reemplaza su contenido, no la referencia.
    PRODUCTS.splice(0, PRODUCTS.length, ...productos.map(adaptarProducto));
    renderProductsGrid();
    populateDuelSelects();
    return true;
  } catch (error) {
    console.info('[BuildZone] Catálogo local en uso (API no disponible):', error.message);
    return false;
  }
}

// ── Panel de administración: pestaña Catálogo ──

function renderizarCatalogoAdmin(productos) {
  const tbody = document.querySelector('#admin-catalogo-table tbody');
  if (!tbody) return;

  if (!productos || productos.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4">No hay productos en el catálogo.</td></tr>';
    return;
  }

  tbody.innerHTML = productos
    .map(
      (p) => `
        <tr>
          <td>${escaparHtml(p.nombre)}<br><span class="table-subtext">${escaparHtml(p.descripcion || '')}</span></td>
          <td>${escaparHtml(p.nombreMarca)}</td>
          <td>${escaparHtml(p.nombreCategoria)}</td>
          <td class="admin-table-actions">
            <button class="btn-mini" data-editar-producto-id="${p.idProducto}">Editar</button>
            <button class="btn-mini btn-mini-danger" data-eliminar-producto-id="${p.idProducto}">Eliminar</button>
          </td>
        </tr>`
    )
    .join('');
}

function llenarSelect(select, opciones, campoId, seleccionado) {
  select.innerHTML = ['<option value="">Selecciona…</option>']
    .concat(
      opciones.map(
        (o) => `<option value="${o[campoId]}" ${o[campoId] === seleccionado ? 'selected' : ''}>${escaparHtml(o.nombre)}</option>`
      )
    )
    .join('');
}

async function cargarCatalogoAdmin() {
  const tbody = document.querySelector('#admin-catalogo-table tbody');
  if (tbody) tbody.innerHTML = '<tr><td colspan="4">Cargando…</td></tr>';
  try {
    catalogoAdminCache = await listarProductos();
    renderizarCatalogoAdmin(catalogoAdminCache);
  } catch (error) {
    if (tbody) tbody.innerHTML = `<tr><td colspan="4">Error al cargar el catálogo: ${escaparHtml(error.message)}</td></tr>`;
  }
}

async function abrirModalProducto(producto) {
  const modal = document.getElementById('producto-modal');
  if (!modal) return;

  let marcas = [];
  let categorias = [];
  try {
    [marcas, categorias] = await Promise.all([listarMarcas(), listarCategorias()]);
  } catch (error) {
    mostrarError(error.message);
    return;
  }

  document.getElementById('producto-modal-title').textContent = producto ? 'Editar producto' : 'Nuevo producto';
  document.getElementById('producto-id').value = producto ? producto.idProducto : '';
  document.getElementById('producto-nombre').value = producto ? producto.nombre : '';
  document.getElementById('producto-descripcion').value = producto ? producto.descripcion || '' : '';
  document.getElementById('producto-imagen').value = producto ? producto.imagen || '' : '';
  llenarSelect(document.getElementById('producto-marca'), marcas, 'idMarca', producto ? producto.idMarca : null);
  llenarSelect(document.getElementById('producto-categoria'), categorias, 'idCategoria', producto ? producto.idCategoria : null);

  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

function cerrarModalProducto() {
  const modal = document.getElementById('producto-modal');
  if (!modal) return;
  modal.classList.remove('active');
  document.body.style.overflow = '';
}

/** Tras cualquier cambio se refresca la tabla admin y el catálogo público. */
async function refrescarCatalogos() {
  await Promise.all([cargarCatalogoAdmin(), cargarCatalogoDesdeApi()]);
}

async function manejarEnvioProducto(event) {
  event.preventDefault();
  const id = document.getElementById('producto-id').value;
  const datos = {
    nombre: document.getElementById('producto-nombre').value.trim(),
    descripcion: document.getElementById('producto-descripcion').value.trim(),
    imagen: document.getElementById('producto-imagen').value.trim() || null,
    idMarca: Number(document.getElementById('producto-marca').value) || null,
    idCategoria: Number(document.getElementById('producto-categoria').value) || null,
  };
  const boton = document.getElementById('producto-form-submit');

  try {
    if (boton) boton.disabled = true;
    if (id) {
      await actualizarProducto(Number(id), datos);
      mostrarExito('Producto actualizado.');
    } else {
      await crearProducto(datos);
      mostrarExito('Producto creado.');
    }
    cerrarModalProducto();
    await refrescarCatalogos();
  } catch (error) {
    mostrarError(error.message);
  } finally {
    if (boton) boton.disabled = false;
  }
}

async function crearDesdeFormularioRapido(inputId, crear, etiqueta) {
  const input = document.getElementById(inputId);
  const nombre = input ? input.value.trim() : '';
  if (!nombre) {
    mostrarError(`Escribe el nombre de la ${etiqueta}.`);
    return;
  }
  try {
    await crear({ nombre, descripcion: null });
    mostrarExito(`${etiqueta.charAt(0).toUpperCase()}${etiqueta.slice(1)} "${nombre}" creada.`);
    input.value = '';
  } catch (error) {
    mostrarError(error.message);
  }
}

async function manejarClicCatalogoAdmin(event) {
  if (event.target.closest('#btn-nuevo-producto')) {
    abrirModalProducto(null);
    return;
  }
  if (event.target.closest('#btn-nueva-marca')) {
    crearDesdeFormularioRapido('nueva-marca-nombre', crearMarca, 'marca');
    return;
  }
  if (event.target.closest('#btn-nueva-categoria')) {
    crearDesdeFormularioRapido('nueva-categoria-nombre', crearCategoria, 'categoría');
    return;
  }

  const botonEditar = event.target.closest('[data-editar-producto-id]');
  if (botonEditar) {
    const producto = catalogoAdminCache.find((p) => p.idProducto === Number(botonEditar.dataset.editarProductoId));
    abrirModalProducto(producto || null);
    return;
  }

  const botonEliminar = event.target.closest('[data-eliminar-producto-id]');
  if (botonEliminar) {
    if (!confirm('¿Eliminar este producto del catálogo? Esta acción no se puede deshacer.')) return;
    botonEliminar.disabled = true;
    try {
      await eliminarProducto(Number(botonEliminar.dataset.eliminarProductoId));
      mostrarExito('Producto eliminado.');
      await refrescarCatalogos();
    } catch (error) {
      mostrarError(error.message);
      botonEliminar.disabled = false;
    }
  }
}

function inicializarCatalogo() {
  cargarCatalogoDesdeApi();

  const panel = document.getElementById('admin-panel-catalogo');
  if (panel) panel.addEventListener('click', manejarClicCatalogoAdmin);

  const form = document.getElementById('producto-form');
  if (form) form.addEventListener('submit', manejarEnvioProducto);

  const modal = document.getElementById('producto-modal');
  if (modal) {
    modal.addEventListener('click', (event) => {
      if (event.target === modal || event.target.closest('[data-close-producto-modal]')) {
        cerrarModalProducto();
      }
    });
  }
}

window.inicializarCatalogo = inicializarCatalogo;
window.cargarCatalogoAdmin = cargarCatalogoAdmin;
window.cargarCatalogoDesdeApi = cargarCatalogoDesdeApi;

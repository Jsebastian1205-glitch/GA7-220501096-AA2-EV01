// Cambia la vista activa entre las páginas del sitio
function showPage(page) {
  // Protege las páginas que requieren sesión (o rol ADMIN) de un acceso
  // directo: si auth.js todavía no cargó, se asume que no hay sesión.
  if (page === 'cuenta' && !(window.estaAutenticado && window.estaAutenticado())) {
    if (typeof mostrarError === 'function') mostrarError('Inicia sesión para ver tu cuenta.');
    toggleLogin(true);
    return;
  }
  if (page === 'admin' && !(window.esAdmin && window.esAdmin())) {
    if (typeof mostrarError === 'function') mostrarError('No tienes permisos de administrador.');
    page = 'inicio';
  }

  const pages = document.querySelectorAll('.page');
  pages.forEach((panel) => panel.classList.remove('active'));

  const targetPage = document.getElementById(`page-${page}`);
  if (targetPage) {
    targetPage.classList.add('active');
  } else {
    document.getElementById('page-inicio')?.classList.add('active');
  }

  document.querySelectorAll('.nav-link').forEach((button) => {
    button.classList.remove('active');
  });

  const activeButton = document.getElementById(`nav-${page}`);
  if (activeButton) {
    activeButton.classList.add('active');
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Muestra u oculta el modal de inicio de sesión
function toggleLogin(show) {
  const modal = document.getElementById('login-modal');
  if (!modal) return;

  modal.classList.toggle('active', show);
  document.body.style.overflow = show ? 'hidden' : '';
}

// Muestra u oculta el modal de registro
function toggleRegister(show) {
  const modal = document.getElementById('register-modal');
  if (!modal) return;

  modal.classList.toggle('active', show);
  document.body.style.overflow = show ? 'hidden' : '';
}

// Muestra u oculta el modal de lectura de artículos
function toggleArticle(show) {
  const modal = document.getElementById('article-modal');
  if (!modal) return;

  modal.classList.toggle('active', show);
  document.body.style.overflow = show ? 'hidden' : '';
}

// Llena el modal de artículo con el contenido correspondiente y lo abre
function openArticle(articleId) {
  const article = ARTICLES[articleId];
  if (!article) return;

  document.getElementById('article-icon').textContent = article.icon;
  document.getElementById('article-tag').textContent = article.tag;
  document.getElementById('article-title').textContent = article.title;
  document.getElementById('article-body').textContent = article.body;

  toggleArticle(true);
}

// Datos de prueba de productos (más adelante se conectarán a la base de datos)
const PRODUCTS = [
  { id: 'i5-14600k', category: 'cpu', brand: 'Intel', name: 'Core i5-14600K', price: 320000, icon: '🕹️',
    stats: { potencia: 78, eficiencia: 70, valor: 88, popularidad: 90 } },
  { id: 'i7-14700k', category: 'cpu', brand: 'Intel', name: 'Core i7-14700K', price: 480000, icon: '🕹️',
    stats: { potencia: 88, eficiencia: 65, valor: 75, popularidad: 85 } },
  { id: 'i9-14900k', category: 'cpu', brand: 'Intel', name: 'Core i9-14900K', price: 650000, icon: '🕹️',
    stats: { potencia: 98, eficiencia: 55, valor: 60, popularidad: 80 } },
  { id: 'r5-7600x', category: 'cpu', brand: 'AMD', name: 'Ryzen 5 7600X', price: 300000, icon: '🕹️',
    stats: { potencia: 75, eficiencia: 85, valor: 90, popularidad: 88 } },
  { id: 'r7-7700x', category: 'cpu', brand: 'AMD', name: 'Ryzen 7 7700X', price: 450000, icon: '🕹️',
    stats: { potencia: 90, eficiencia: 88, valor: 82, popularidad: 95 } },

  { id: 'rtx-4060', category: 'gpu', brand: 'NVIDIA', name: 'RTX 4060', price: 1800000, icon: '🎮',
    stats: { potencia: 70, eficiencia: 82, valor: 80, popularidad: 88 } },
  { id: 'rtx-4070', category: 'gpu', brand: 'NVIDIA', name: 'RTX 4070', price: 2600000, icon: '🎮',
    stats: { potencia: 82, eficiencia: 80, valor: 72, popularidad: 85 } },
  { id: 'rtx-4070ti', category: 'gpu', brand: 'NVIDIA', name: 'RTX 4070 Ti', price: 3600000, icon: '🎮',
    stats: { potencia: 90, eficiencia: 75, valor: 60, popularidad: 78 } },
  { id: 'rx-7600', category: 'gpu', brand: 'AMD', name: 'Radeon RX 7600', price: 1500000, icon: '🎮',
    stats: { potencia: 65, eficiencia: 78, valor: 85, popularidad: 75 } },
  { id: 'rx-7800xt', category: 'gpu', brand: 'AMD', name: 'Radeon RX 7800 XT', price: 2900000, icon: '🎮',
    stats: { potencia: 85, eficiencia: 76, valor: 78, popularidad: 82 } },

  { id: 'kingston-fury-16', category: 'ram', brand: 'Kingston', name: 'Fury Beast 16GB 3200MHz', price: 220000, icon: '💾',
    stats: { potencia: 55, eficiencia: 70, valor: 92, popularidad: 85 } },
  { id: 'corsair-vengeance-32', category: 'ram', brand: 'Corsair', name: 'Vengeance 32GB 6000MHz', price: 480000, icon: '💾',
    stats: { potencia: 82, eficiencia: 80, valor: 75, popularidad: 90 } },
  { id: 'gskill-tridentz5-32', category: 'ram', brand: 'G.Skill', name: 'Trident Z5 32GB 6400MHz', price: 620000, icon: '💾',
    stats: { potencia: 90, eficiencia: 78, valor: 60, popularidad: 80 } },
  { id: 'corsair-dominator-64', category: 'ram', brand: 'Corsair', name: 'Dominator Platinum 64GB 5600MHz', price: 950000, icon: '💾',
    stats: { potencia: 88, eficiencia: 85, valor: 55, popularidad: 76 } },
];

const CATEGORY_LABELS = { cpu: 'Procesador', gpu: 'Tarjeta Gráfica', ram: 'Memoria RAM' };
const STAT_LABELS = { potencia: 'Potencia', eficiencia: 'Eficiencia', valor: 'Valor', popularidad: 'Popularidad' };

// Contenido de prueba de los artículos del blog
const ARTICLES = {
  'cpu-guide': { icon: '🖥️', tag: 'Guías', title: 'Cómo elegir el mejor procesador para tu PC en 2025',
    body: 'Antes de comprar un procesador define primero para qué lo vas a usar: gaming, edición de video o trabajo de oficina. Compara núcleos, hilos y velocidad, y no olvides revisar que sea compatible con tu placa madre antes de decidir.' },
  'build-guide': { icon: '🔧', tag: 'Tutorial', title: 'Guía para armar tu PC paso a paso sin errores',
    body: 'Arma tu PC en orden: primero el procesador y la RAM sobre la placa madre, luego móntala en el gabinete, conecta la fuente de poder y por último la tarjeta gráfica. Revisa cada conexión antes de encender el equipo por primera vez.' },
  'gpu-2025': { icon: '🎮', tag: 'Novedades', title: 'Nuevas GPUs lanzadas en 2025: análisis completo',
    body: 'Este año llegaron tarjetas gráficas con mejoras notables en eficiencia energética y trazado de rayos. Aquí en BuildZone puedes comparar las especificaciones y precios de los modelos más recientes en la sección de Comparador.' },
  'ram-guide': { icon: '💾', tag: 'Guías', title: 'RAM: ¿cuánta necesitas y qué velocidad elegir?',
    body: 'Para uso general 16GB son suficientes, pero si editas video o usas máquinas virtuales, considera 32GB o más. La velocidad en MHz también influye en el rendimiento, especialmente en procesadores AMD.' },
  'cooling-guide': { icon: '❄️', tag: 'Tutorial', title: 'Refrigeración líquida vs aire: cuál conviene más',
    body: 'La refrigeración por aire es más económica y fácil de mantener, mientras que la líquida ofrece mejores temperaturas en procesadores de alto rendimiento. La elección depende de tu presupuesto y de cuánto exijas a tu equipo.' },
  'amd-vs-intel': { icon: '⚖️', tag: 'Comparativas', title: 'AMD vs Intel en 2025: ¿quién gana en relación precio-rendimiento?',
    body: 'AMD suele destacar en relación precio-rendimiento para gaming, mientras que Intel mantiene ventaja en tareas de un solo núcleo. Usa el Comparador de BuildZone para ver las estadísticas lado a lado antes de decidir.' },
};

let currentDuelCategory = 'cpu';
let currentProductCategory = 'all';
let currentProductSearch = '';

// ── Comparador (duelo) ──

function setDuelCategory(category) {
  currentDuelCategory = category;

  document.querySelectorAll('.duel-cat-btn').forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.category === category);
  });

  populateDuelSelects();

  const arena = document.getElementById('duel-arena');
  if (arena) {
    arena.innerHTML = '<div class="duel-empty">Selecciona 2 productos para comenzar el duelo ⚔️</div>';
  }
}

function populateDuelSelects() {
  const selectA = document.getElementById('duel-select-a');
  const selectB = document.getElementById('duel-select-b');
  if (!selectA || !selectB) return;

  const categoryProducts = PRODUCTS.filter((p) => p.category === currentDuelCategory);
  const options = ['<option value="">Selecciona un producto…</option>']
    .concat(categoryProducts.map((p) => `<option value="${p.id}">${p.brand} ${p.name}</option>`))
    .join('');

  selectA.innerHTML = options;
  selectB.innerHTML = options;
}

function buildDuelCard(product, opponent) {
  const statsHtml = Object.entries(product.stats).map(([key, value]) => {
    const isWinner = value > opponent.stats[key];
    return `
      <div class="duel-stat">
        <div class="duel-stat-label">${STAT_LABELS[key]}${isWinner ? ' 🏆' : ''}</div>
        <div class="duel-stat-track"><div class="duel-stat-fill ${isWinner ? 'duel-stat-fill-win' : ''}" style="width:${value}%"></div></div>
        <div class="duel-stat-value">${value}</div>
      </div>`;
  }).join('');

  return `
    <div class="duel-card">
      <div class="duel-card-badge">${CATEGORY_LABELS[product.category]}</div>
      <div class="duel-card-icon">${product.icon}</div>
      <div class="duel-card-brand">${product.brand}</div>
      <div class="duel-card-name">${product.name}</div>
      <div class="duel-card-price">$${product.price.toLocaleString('es-CO')}</div>
      <div class="duel-card-stats">${statsHtml}</div>
      <div class="duel-card-seal">⬡ BuildZone</div>
    </div>`;
}

function renderDuel() {
  const arena = document.getElementById('duel-arena');
  const selectA = document.getElementById('duel-select-a');
  const selectB = document.getElementById('duel-select-b');
  if (!arena || !selectA || !selectB) return;

  const productA = PRODUCTS.find((p) => p.id === selectA.value);
  const productB = PRODUCTS.find((p) => p.id === selectB.value);

  if (!productA || !productB) {
    arena.innerHTML = '<div class="duel-empty">Selecciona 2 productos para comenzar el duelo ⚔️</div>';
    return;
  }

  if (productA.id === productB.id) {
    arena.innerHTML = '<div class="duel-empty">Elige dos productos diferentes para comparar</div>';
    return;
  }

  arena.innerHTML = `
    ${buildDuelCard(productA, productB)}
    <div class="duel-vs-badge">VS</div>
    ${buildDuelCard(productB, productA)}
  `;
}

// ── Página de Productos (filtros y búsqueda) ──

function setProductCategory(category) {
  currentProductCategory = category;

  document.querySelectorAll('#products-categories .cat-card').forEach((card) => {
    card.classList.toggle('active', card.dataset.category === category);
  });

  renderProductsGrid();
}

function renderProductsGrid() {
  const grid = document.getElementById('products-grid');
  if (!grid) return;

  const search = currentProductSearch.trim().toLowerCase();
  const filtered = PRODUCTS.filter((p) => {
    const matchesCategory = currentProductCategory === 'all' || p.category === currentProductCategory;
    const matchesSearch = `${p.brand} ${p.name}`.toLowerCase().includes(search);
    return matchesCategory && matchesSearch;
  });

  if (filtered.length === 0) {
    grid.innerHTML = '<div class="products-empty">No encontramos productos con esos filtros. Prueba con otra categoría o búsqueda.</div>';
    return;
  }

  grid.innerHTML = filtered.map((p) => `
    <div class="product-card">
      <div class="product-card-icon">${p.icon}</div>
      <div class="product-card-brand">${p.brand}</div>
      <div class="product-card-name">${p.name}</div>
      <div class="product-card-price">$${p.price.toLocaleString('es-CO')}</div>
    </div>
  `).join('');
}

// Inicializa los eventos cuando la página termina de cargar
document.addEventListener('DOMContentLoaded', () => {
  // Cierra los modales al hacer clic fuera de su contenido
  const loginModal = document.getElementById('login-modal');
  if (loginModal) {
    loginModal.addEventListener('click', (event) => {
      if (event.target === loginModal) {
        toggleLogin(false);
      }
    });
  }

  const registerModal = document.getElementById('register-modal');
  if (registerModal) {
    registerModal.addEventListener('click', (event) => {
      if (event.target === registerModal) {
        toggleRegister(false);
      }
    });
  }

  const articleModal = document.getElementById('article-modal');
  if (articleModal) {
    articleModal.addEventListener('click', (event) => {
      if (event.target === articleModal) {
        toggleArticle(false);
      }
    });
  }

  document.querySelectorAll('[data-article]').forEach((card) => {
    card.addEventListener('click', () => openArticle(card.dataset.article));
  });

  // Los botones de login pueden abrir el modal directamente, o bien delegar
  // en auth.js (si el usuario ya inició sesión, auth.js reemplaza este
  // comportamiento por el menú desplegable de cuenta).
  document.querySelectorAll('[data-login]').forEach((button) => {
    button.addEventListener('click', () => toggleLogin(true));
  });

  document.querySelectorAll('[data-open-register]').forEach((el) => {
    el.addEventListener('click', () => {
      toggleLogin(false);
      toggleRegister(true);
    });
  });

  document.querySelectorAll('[data-open-login]').forEach((el) => {
    el.addEventListener('click', () => {
      toggleRegister(false);
      toggleLogin(true);
    });
  });

  document.querySelectorAll('.duel-cat-btn').forEach((btn) => {
    btn.addEventListener('click', () => setDuelCategory(btn.dataset.category));
  });

  populateDuelSelects();
  const selectDuelA = document.getElementById('duel-select-a');
  const selectDuelB = document.getElementById('duel-select-b');
  if (selectDuelA) selectDuelA.addEventListener('change', renderDuel);
  if (selectDuelB) selectDuelB.addEventListener('change', renderDuel);

  document.querySelectorAll('#products-categories .cat-card').forEach((card) => {
    card.addEventListener('click', () => setProductCategory(card.dataset.category));
  });

  const productsSearch = document.getElementById('products-search');
  if (productsSearch) {
    productsSearch.addEventListener('input', (event) => {
      currentProductSearch = event.target.value;
      renderProductsGrid();
    });
  }
  renderProductsGrid();

  // Formulario de contacto: no hay endpoint de backend para esto todavía,
  // así que solo confirmamos el envío al usuario y limpiamos el formulario.
  const contactoForm = document.getElementById('contacto-form');
  if (contactoForm) {
    contactoForm.addEventListener('submit', (event) => {
      event.preventDefault();
      if (typeof mostrarExito === 'function') {
        mostrarExito('¡Mensaje enviado! Te responderemos pronto.');
      }
      contactoForm.reset();
    });
  }

  // Punto de enganche para los módulos nuevos (auth.js / planes.js / cuenta.js
  // / admin.js): cada uno expone su propio inicializador solo si el script
  // correspondiente fue cargado, así este archivo no depende directamente
  // de ellos ni se rompe si alguno falta.
  if (typeof window.inicializarAuth === 'function') window.inicializarAuth();
  if (typeof window.inicializarPlanes === 'function') window.inicializarPlanes();
  if (typeof window.inicializarCuenta === 'function') window.inicializarCuenta();
  if (typeof window.inicializarAdmin === 'function') window.inicializarAdmin();

  showPage('inicio');
});

window.showPage = showPage;
window.toggleLogin = toggleLogin;
window.toggleRegister = toggleRegister;
window.toggleArticle = toggleArticle;

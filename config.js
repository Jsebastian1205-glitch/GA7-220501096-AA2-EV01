/**
 * Configuración del front-end por ambiente.
 *
 * - En desarrollo (abriendo index.html desde el disco, con Live Server o en
 *   localhost) la API se busca en http://localhost:8080/api.
 * - En producción (GitHub Pages u otro dominio) se usa API_URL_PRODUCCION.
 *
 * Después de desplegar la API en Render, reemplace la URL de abajo por la
 * que Render le asigne a su servicio (termina en .onrender.com) + "/api".
 * Debe cargarse antes que api.js en index.html.
 */
const API_URL_DESARROLLO = 'http://localhost:8080/api';
const API_URL_PRODUCCION = 'https://buildzone-api.onrender.com/api';

const ES_AMBIENTE_LOCAL =
  location.protocol === 'file:' ||
  ['localhost', '127.0.0.1', ''].includes(location.hostname);

window.BUILDZONE_CONFIG = {
  ambiente: ES_AMBIENTE_LOCAL ? 'desarrollo' : 'produccion',
  apiUrl: ES_AMBIENTE_LOCAL ? API_URL_DESARROLLO : API_URL_PRODUCCION,
};

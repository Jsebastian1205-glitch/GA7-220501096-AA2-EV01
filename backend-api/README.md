# buildzone-api

API REST del catalogo de BuildZone (Producto, Marca, Categoria), construida
con Spring Boot sobre la misma base de datos que ya usa el modulo de
consola JDBC (`backend-java`) del proyecto formativo **Proyecto BuildZone**.

Este proyecto **no reemplaza** a `backend-java`: es una capa REST adicional
e independiente sobre los mismos datos (las tablas `producto`, `marca` y
`categoria`), pensada para ser consumida por un front-end u otro cliente
HTTP, en vez de por un menu de consola. Vive en su propia carpeta
(`backend-api`) dentro del mismo repositorio de `Proyecto BuildZone`, sin
modificar nada de `backend-java`.

## Tecnologias

| Capa | Tecnologia |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3 (Spring Web, Spring Data JPA) |
| Validacion | Bean Validation (`@NotBlank`, `@Size`, `@NotNull`) |
| Base de datos | MySQL/MariaDB (XAMPP) — base de datos `buildzone` ya existente |
| Build | Maven |
| Control de versiones | Git |

## Estructura del proyecto

```
buildzone-api/
├── pom.xml
├── src/main/resources/application.properties   Configuracion (puerto, BD)
└── src/main/java/com/buildzone/api/
    ├── BuildzoneApiApplication.java             Punto de entrada
    ├── model/                                   Entidades JPA
    │   ├── Producto.java
    │   ├── Marca.java
    │   └── Categoria.java
    ├── repository/                              Acceso a datos (Spring Data)
    │   ├── ProductoRepository.java
    │   ├── MarcaRepository.java
    │   └── CategoriaRepository.java
    ├── dto/                                      Objetos de entrada/salida
    │   ├── ProductoRequest.java / ProductoResponse.java
    │   ├── MarcaRequest.java / MarcaResponse.java
    │   └── CategoriaRequest.java / CategoriaResponse.java
    ├── service/                                  Reglas de negocio
    │   ├── ProductoService.java (+ impl)
    │   ├── MarcaService.java (+ impl)
    │   └── CategoriaService.java (+ impl)
    ├── controller/                                Endpoints REST
    │   ├── ProductoController.java
    │   ├── MarcaController.java
    │   └── CategoriaController.java
    └── exception/                                Manejo centralizado de errores
        ├── GlobalExceptionHandler.java
        ├── RecursoNoEncontradoException.java
        └── RecursoDuplicadoException.java
```

## Estandares de codificacion aplicados

- Paquetes en minuscula por responsabilidad (`model`, `repository`, `dto`,
  `service`, `controller`, `exception`).
- Clases en PascalCase, metodos y variables en camelCase con nombres que
  describen la accion (`listar`, `crear`, `actualizar`, `eliminar`,
  `buscarOFallar`).
- Separacion en capas: Controller (HTTP) -> Service (reglas de negocio) ->
  Repository (persistencia), programando contra interfaces
  (`MarcaService`, no `MarcaServiceImpl`, en las dependencias).
- DTOs de entrada validados con Bean Validation; nunca se expone la
  entidad JPA directamente en la respuesta (se usa un DTO de respuesta).
- Comentarios Javadoc en cada clase y metodo explicando su proposito.
- Manejo centralizado de errores con `@RestControllerAdvice`, para que
  cada tipo de error (validacion, recurso no encontrado, nombre
  duplicado, violacion de llave foranea) tenga una respuesta HTTP
  consistente en vez del error generico de Spring.

## Endpoints de la API

Base URL local: `http://localhost:8082`

### Marca — `/api/marcas`

| Metodo | Ruta | Descripcion | Body | Respuesta exitosa |
|---|---|---|---|---|
| GET | `/api/marcas` | Lista todas las marcas | — | 200 OK, arreglo de marcas |
| GET | `/api/marcas/{id}` | Consulta una marca por id | — | 200 OK, o 404 si no existe |
| POST | `/api/marcas` | Crea una marca | `{"nombre": "...", "descripcion": "..."}` | 201 Created |
| PUT | `/api/marcas/{id}` | Actualiza una marca | `{"nombre": "...", "descripcion": "..."}` | 200 OK, o 404 si no existe |
| DELETE | `/api/marcas/{id}` | Elimina una marca | — | 204 No Content, o 404/409 |

Ejemplo de respuesta (`GET /api/marcas/1`):
```json
{ "idMarca": 1, "nombre": "Nike", "descripcion": "Ropa y calzado deportivo" }
```

### Categoria — `/api/categorias`

Mismos 5 endpoints que Marca, reemplazando `/api/marcas` por
`/api/categorias` y `idMarca` por `idCategoria`.

### Producto — `/api/productos`

| Metodo | Ruta | Descripcion | Body | Respuesta exitosa |
|---|---|---|---|---|
| GET | `/api/productos` | Lista todos los productos | — | 200 OK, arreglo de productos |
| GET | `/api/productos/{id}` | Consulta un producto por id | — | 200 OK, o 404 si no existe |
| POST | `/api/productos` | Crea un producto | ver abajo | 201 Created |
| PUT | `/api/productos/{id}` | Actualiza un producto | ver abajo | 200 OK, o 404 |
| DELETE | `/api/productos/{id}` | Elimina un producto | — | 204 No Content, o 404 |

Body de `POST`/`PUT /api/productos`:
```json
{
  "nombre": "Camiseta deportiva",
  "descripcion": "Camiseta transpirable talla M",
  "imagen": "camiseta.jpg",
  "idMarca": 1,
  "idCategoria": 2
}
```

Respuesta (incluye el nombre de marca/categoria, no solo el id):
```json
{
  "idProducto": 5,
  "nombre": "Camiseta deportiva",
  "descripcion": "Camiseta transpirable talla M",
  "imagen": "camiseta.jpg",
  "idMarca": 1,
  "nombreMarca": "Nike",
  "idCategoria": 2,
  "nombreCategoria": "Ropa"
}
```

### Codigos de error comunes

| Codigo | Cuando ocurre |
|---|---|
| 400 Bad Request | Datos invalidos (nombre vacio, id de marca/categoria faltante, etc.) |
| 404 Not Found | Se pide/actualiza/elimina un id que no existe |
| 409 Conflict | Nombre de marca/categoria duplicado, o se intenta eliminar una marca/categoria que todavia tiene productos asociados |

## Como ejecutarlo

Requiere Java 21, Maven y MySQL/MariaDB corriendo (XAMPP, usuario `root`
sin contrasena). La base de datos `buildzone` y sus tablas ya deben
existir (las crea/usa `backend-java`); esta API solo lee y escribe sobre
ellas, no las crea.

```bash
mvn spring-boot:run
```

El servicio queda disponible en `http://localhost:8082` (puerto distinto
al de `auth-service`, que usa 8081, para poder correr ambos a la vez).

## Control de versiones

Este proyecto se agrega como una carpeta nueva (`backend-api`) dentro del
repositorio Git que ya existe para `Proyecto BuildZone`, sin tocar
`backend-java`:

```bash
git add backend-api
git commit -m "feat: API REST de catalogo (Producto, Marca, Categoria) - backend-api"
git push
```

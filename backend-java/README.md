# BuildZone - Modulo de gestion (JDBC)

Modulo de codificacion del proyecto BuildZone / PC Compare (Proyecto SENA 1).
Implementa la capa de persistencia con JDBC puro (sin ORM) y las operaciones
de **insercion, consulta, actualizacion y eliminacion (CRUD)** para las
entidades Producto, Marca y Categoria, siguiendo el diagrama de clases y las
historias de usuario definidas en las etapas anteriores del ciclo de vida
del software.

Este es el unico backend de trabajo del proyecto: vive dentro de
`C:\Proyecto Sena 1\backend-java` junto con el front-end (en la raiz de
`Proyecto Sena 1`) y las pruebas E2E (`Proyecto Sena 1\validacion`).

## Estructura del proyecto

```
Proyecto Sena 1/
├── index.html, Style.css, *.js          Front-end
├── validacion/                          Pruebas E2E (Playwright)
└── backend-java/                        Este modulo
    ├── pom.xml
    ├── src/main/resources/schema.sql    Referencia de tablas (no ejecutar)
    └── src/main/java/com/buildzone/
        ├── app/
        │   └── Main.java                Menu de consola (punto de entrada)
        ├── config/
        │   └── DatabaseConnection.java  Conexion JDBC (MySQL/XAMPP)
        ├── dao/
        │   ├── ProductDAO.java          CRUD de producto
        │   ├── BrandDAO.java            CRUD de marca
        │   └── CategoryDAO.java         CRUD de categoria
        └── model/
            ├── Product.java
            ├── Brand.java
            └── Category.java
```

## Estandares de codificacion aplicados

- **Paquetes**: minusculas, jerarquia por responsabilidad
  (`com.buildzone.model`, `com.buildzone.dao`, `com.buildzone.config`,
  `com.buildzone.app`).
- **Clases**: PascalCase y sustantivos (`Product`, `ProductDAO`,
  `DatabaseConnection`).
- **Metodos**: camelCase con verbo + entidad (`insertProduct`,
  `getAllProducts`, `updateProduct`, `deleteProduct`).
- **Variables**: camelCase descriptivo (`idProduct`, `idBrand`,
  `description`).
- **Constantes**: MAYUSCULAS_CON_GUION_BAJO (`URL`, `USER`, `PASSWORD`).

## Configuracion de la base de datos

El proyecto se conecta a MySQL/MariaDB servido por XAMPP (`localhost:3306`,
usuario `root`, sin contrasena). La base de datos `buildzone` ya existe en
el equipo de desarrollo con su modelo completo (10 tablas); este modulo usa
puntualmente `producto`, `marca` y `categoria`, cuyas columnas fueron
verificadas contra la estructura real en phpMyAdmin.

`src/main/resources/schema.sql` queda solo como referencia de esas tres
tablas y **no debe ejecutarse** sobre la base existente, ya que recrearia
tablas que ya tienen datos.

## Ejecucion

Desde NetBeans: abra el proyecto apuntando a la carpeta `backend-java`
(reconoce el `pom.xml`) y ejecute `Main.java` (`com.buildzone.app.Main`).

Desde la terminal, con Maven instalado, dentro de `backend-java`:

```
mvn compile exec:java
```

## Control de versiones

El proyecto se versiona con Git. Realice commits pequenos y descriptivos por
cada funcionalidad (por ejemplo: `feat: CRUD de productos`, `feat: CRUD de
marcas`, `docs: script de base de datos`).

-- =====================================================================
-- BuildZone API - Script 02 (OPCIONAL): datos de ejemplo del catalogo
--
-- Solo para una base "buildzone" de pruebas cuyo catalogo este vacio.
-- Usa INSERT IGNORE: si la marca/categoria ya existe (nombre UNIQUE) no
-- se duplica. No lo ejecute sobre datos reales que no quiera mezclar.
-- =====================================================================

USE buildzone;

INSERT IGNORE INTO categoria (nombre, descripcion) VALUES
    ('Procesador', 'CPU de escritorio'),
    ('Tarjeta Grafica', 'GPU dedicadas'),
    ('Memoria RAM', 'Modulos DDR4 y DDR5');

INSERT IGNORE INTO marca (nombre, descripcion) VALUES
    ('Intel', NULL), ('AMD', NULL), ('NVIDIA', NULL),
    ('Kingston', NULL), ('Corsair', NULL), ('G.Skill', NULL);

INSERT INTO producto (id_marca, id_categoria, nombre, descripcion, imagen)
SELECT m.id_marca, c.id_categoria, p.nombre, p.descripcion, NULL
FROM (
    SELECT 'Intel' AS marca, 'Procesador' AS categoria, 'Core i5-14600K' AS nombre, '14 nucleos (6P+8E), hasta 5.3 GHz' AS descripcion
    UNION ALL SELECT 'Intel', 'Procesador', 'Core i7-14700K', '20 nucleos (8P+12E), hasta 5.6 GHz'
    UNION ALL SELECT 'Intel', 'Procesador', 'Core i9-14900K', '24 nucleos (8P+16E), hasta 6.0 GHz'
    UNION ALL SELECT 'AMD', 'Procesador', 'Ryzen 5 7600X', '6 nucleos / 12 hilos, hasta 5.3 GHz'
    UNION ALL SELECT 'AMD', 'Procesador', 'Ryzen 7 7700X', '8 nucleos / 16 hilos, hasta 5.4 GHz'
    UNION ALL SELECT 'NVIDIA', 'Tarjeta Grafica', 'RTX 4060', '8 GB GDDR6, DLSS 3'
    UNION ALL SELECT 'NVIDIA', 'Tarjeta Grafica', 'RTX 4070', '12 GB GDDR6X, DLSS 3'
    UNION ALL SELECT 'NVIDIA', 'Tarjeta Grafica', 'RTX 4070 Ti', '12 GB GDDR6X, DLSS 3'
    UNION ALL SELECT 'AMD', 'Tarjeta Grafica', 'Radeon RX 7600', '8 GB GDDR6, FSR 3'
    UNION ALL SELECT 'AMD', 'Tarjeta Grafica', 'Radeon RX 7800 XT', '16 GB GDDR6, FSR 3'
    UNION ALL SELECT 'Kingston', 'Memoria RAM', 'Fury Beast 16GB 3200MHz', 'DDR4, CL16'
    UNION ALL SELECT 'Corsair', 'Memoria RAM', 'Vengeance 32GB 6000MHz', 'DDR5, CL36, 2x16 GB'
    UNION ALL SELECT 'G.Skill', 'Memoria RAM', 'Trident Z5 32GB 6400MHz', 'DDR5, CL32, 2x16 GB'
    UNION ALL SELECT 'Corsair', 'Memoria RAM', 'Dominator Platinum 64GB 5600MHz', 'DDR5, CL40, 2x32 GB'
) AS p
JOIN marca m ON m.nombre = p.marca
JOIN categoria c ON c.nombre = p.categoria
WHERE NOT EXISTS (SELECT 1 FROM producto x WHERE x.nombre = p.nombre);

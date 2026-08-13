-- Ejecuta este archivo una sola vez desde el Query Tool de pgAdmin 4.
-- Debes estar conectado a la base administrativa "postgres".

-- Crea la única base de datos que utilizará todo el sistema institucional.
CREATE DATABASE gestion_utng
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

-- Después de crearla, abre un Query Tool conectado a "gestion_utng".
-- Las tablas se crearán automáticamente al iniciar la aplicación.

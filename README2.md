# Gestión Institucional UTNG
Aplicación JavaFX conectada a una sola base PostgreSQL llamada gestion_utng. Las pantallas comparten la misma conexión y cada módulo utiliza tablas distintas, relacionadas mediante llaves foráneas.

# 1. Crear la base en pgAdmin 4
Abre pgAdmin 4 y conéctate a tu servidor PostgreSQL.
Selecciona la base administrativa postgres.
Abre Query Tool.
Ejecuta crear_base_datos.sql.
Refresca Databases y comprueba que aparezca gestion_utng.
No necesitas ejecutar manualmente schema.sql. Al iniciar la aplicación, DatabaseInitializer crea las tablas, índices y datos iniciales de forma repetible. Si una tabla ya existe, no se duplica.

# 2. Configurar la conexión
Edita config/database.properties y escribe únicamente tu contraseña local:

db.url=jdbc:postgresql://localhost:5432/gestion_utng
db.user=postgres
db.password=TU_CONTRASENA_REAL
db.initialize=true
Este archivo está excluido mediante .gitignore. No publiques ni envíes la contraseña dentro de un controlador, FXML o repositorio.

En producción también puedes utilizar estas variables de entorno:

UTNG_DB_URL
UTNG_DB_USER
UTNG_DB_PASSWORD
UTNG_DB_INITIALIZE
UTNG_PG_BIN (opcional, carpeta bin de PostgreSQL)
# 3. Ejecutar
Con Maven instalado:

mvn clean javafx:run
En este equipo Maven no está agregado al PATH, pero las dependencias ya están en .m2. Puedes ejecutar:

powershell -ExecutionPolicy Bypass -File scripts\compilar-ejecutar.ps1
Para verificar únicamente la compilación:

powershell -ExecutionPolicy Bypass -File scripts\compilar-ejecutar.ps1
Funciones conectadas
Estadísticas: conteos actuales de equipos, préstamos, órdenes y usuarios.
Inventario: búsqueda, filtros, alta, edición y baja lógica de productos.
Préstamos: registro transaccional, descuento de stock y devoluciones.
Órdenes de servicio: alta, consulta, filtros, inicio, cierre y cancelación.
Mantenimientos: programación y cambios transaccionales de estado del equipo.
Equipos: búsqueda, filtros, alta, consulta y edición.
Usuarios: búsqueda, filtros, alta, edición, baja lógica y exportación CSV.
Reportes: PDF paginado por módulo y ZIP global con seis documentos PDF, periodo, tabla, encabezado UTNG e historial en PostgreSQL.
Respaldos: archivos reales con pg_dump, copia, restauración con pg_restore y eliminación confirmada.
Mi perfil: consulta, edición y cambio de contraseña con PBKDF2.
Inicio de sesión: valida correo, estado y contraseña almacenada.
El administrador inicial es alejandro.herrera@utng.edu.mx. Como el esquema no guarda una contraseña predeterminada, el primer acceso desde la pantalla de login se realiza dejando la contraseña vacía. Después debes crear una contraseña de al menos ocho caracteres en Mi perfil.

Organización del código
database: configuración, conexión e inicialización del esquema.
model: objetos inmutables que representan filas consultadas.
repository: SQL y JDBC mediante PreparedStatement.
service: validaciones, transacciones, reportes y respaldos.
controladores JavaFX: eventos, formularios y actualización visual.
FXML: estructura de cada pantalla, sin archivo CSS externo.
Las clases, campos, métodos, consultas y operaciones importantes incluyen comentarios en español para explicar su responsabilidad y sus decisiones de seguridad.
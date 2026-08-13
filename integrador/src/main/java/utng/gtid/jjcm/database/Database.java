package utng.gtid.jjcm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto central para abrir conexiones JDBC con PostgreSQL.
 *
 * Los repositorios solicitan conexiones a esta clase y siempre las cierran con
 * try-with-resources. No se comparte una Connection global porque una conexión
 * cerrada o una transacción fallida bloquearía toda la aplicación.
 */
public final class Database {

    /** Configuración cargada una sola vez durante la vida de la aplicación. */
    private static final DatabaseConfig CONFIG = DatabaseConfig.load();

    /** Impide crear objetos Database; todos sus métodos son estáticos. */
    private Database() {
    }

    /**
     * Abre una conexión nueva con los datos configurados.
     *
     * @return conexión JDBC abierta.
     * @throws SQLException cuando PostgreSQL no está disponible o rechaza el acceso.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getUrl(),
                CONFIG.getUser(),
                CONFIG.getPassword()
        );
    }

    /**
     * Ejecuta una consulta mínima para comprobar conexión y credenciales.
     */
    public static void testConnection() {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(3)) {
                throw new DatabaseException("PostgreSQL respondió con una conexión no válida.");
            }
        } catch (SQLException error) {
            throw new DatabaseException(
                    "No fue posible conectar con PostgreSQL (" + CONFIG.describeSafely() + ").",
                    error
            );
        }
    }

    /** @return configuración activa para el inicializador y diagnósticos seguros. */
    public static DatabaseConfig getConfig() {
        return CONFIG;
    }
}

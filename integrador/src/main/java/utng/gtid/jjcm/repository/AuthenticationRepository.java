package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.security.PasswordHasher;
import utng.gtid.jjcm.session.SessionContext;

/**
 * Repositorio mínimo de autenticación para el acceso a la aplicación.
 */
public final class AuthenticationRepository {

    /** Consulta solamente usuarios activos por correo normalizado. */
    private static final String FIND_SQL =
            "SELECT u.id, u.nombre, u.apellidos, r.nombre AS rol, u.password_hash "
            + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
            + "WHERE LOWER(u.correo) = ? AND u.activo = TRUE";

    /** Guarda la fecha del inicio de sesión correcto. */
    private static final String ACCESS_SQL =
            "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE id = ?";

    /**
     * Devuelve true para un hash PBKDF2 correcto. Si el administrador inicial
     * todavía no tiene hash, permite una sola modalidad de primer acceso: clave vacía.
     */
    public boolean authenticate(String email, String password) {
        try (Connection connection = Database.getConnection();
             PreparedStatement find = connection.prepareStatement(FIND_SQL)) {
            find.setString(1, email.toLowerCase(Locale.ROOT));
            try (ResultSet result = find.executeQuery()) {
                if (!result.next()) return false;

                long userId = result.getLong("id");
                String storedHash = result.getString("password_hash");
                boolean firstAccess = storedHash == null || storedHash.isBlank();
                boolean valid = firstAccess
                        ? password == null || password.isEmpty()
                        : PasswordHasher.verify(password, storedHash);
                if (!valid) return false;

                registerAccess(connection, userId);

                // La interfaz utilizará estos datos en todas las pantallas.
                SessionContext.start(
                        userId,
                        result.getString("nombre"),
                        result.getString("apellidos"),
                        result.getString("rol")
                );
                return true;
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo validar el inicio de sesión.", error);
        }
    }

    /** Actualiza ultimo_acceso utilizando la misma conexión. */
    private void registerAccess(Connection connection, long userId) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(ACCESS_SQL)) {
            update.setLong(1, userId);
            update.executeUpdate();
        }
    }
}

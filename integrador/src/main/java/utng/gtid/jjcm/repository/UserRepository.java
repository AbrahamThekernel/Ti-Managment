package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;
import utng.gtid.jjcm.model.UserView;

/**
 * Repositorio JDBC de usuarios, roles y perfil del administrador inicial.
 */
public final class UserRepository {

    /** Columnas comunes utilizadas para construir UserView. */
    private static final String USER_COLUMNS =
            "u.id, u.rol_id, r.nombre AS rol, u.nombre, u.apellidos, u.correo, "
            + "COALESCE(u.telefono, '') AS telefono, "
            + "COALESCE(u.puesto, '') AS puesto, "
            + "COALESCE(u.departamento, '') AS departamento, "
            + "u.activo, u.ultimo_acceso ";

    /** Consulta ordenada de todos los usuarios. */
    private static final String FIND_ALL_SQL =
            "SELECT " + USER_COLUMNS
            + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
            + "ORDER BY u.nombre, u.apellidos";

    /** Consulta un usuario concreto para que Mi perfil respete la sesión. */
    private static final String FIND_BY_ID_SQL =
            "SELECT " + USER_COLUMNS
            + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
            + "WHERE u.id = ?";

    /** Consulta del administrador que representa la sesión provisional. */
    private static final String FIND_ADMIN_SQL =
            "SELECT " + USER_COLUMNS
            + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
            + "WHERE r.nombre = 'ADMINISTRADOR' AND u.activo = TRUE "
            + "ORDER BY u.id LIMIT 1";

    /** Inserción con parámetros para evitar inyección SQL. */
    private static final String INSERT_SQL =
            "INSERT INTO usuarios "
            + "(rol_id, nombre, apellidos, correo, telefono, puesto, departamento, "
            + "password_hash, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)";

    /** Actualización de los datos administrables de un usuario. */
    private static final String UPDATE_SQL =
            "UPDATE usuarios SET rol_id = ?, nombre = ?, apellidos = ?, correo = ?, "
            + "telefono = ?, puesto = ?, departamento = ?, actualizado_en = CURRENT_TIMESTAMP "
            + "WHERE id = ?";

    /** Baja lógica para conservar relaciones históricas. */
    private static final String TOGGLE_SQL =
            "UPDATE usuarios SET activo = NOT activo, actualizado_en = CURRENT_TIMESTAMP "
            + "WHERE id = ?";

    /** Consulta de roles habilitados para el formulario. */
    private static final String ROLES_SQL =
            "SELECT id, nombre, descripcion FROM roles WHERE activo = TRUE ORDER BY nombre";

    /** Consulta del hash actual sin exponerlo en UserView. */
    private static final String PASSWORD_SQL =
            "SELECT password_hash FROM usuarios WHERE id = ?";

    /** Actualización independiente de contraseña. */
    private static final String UPDATE_PASSWORD_SQL =
            "UPDATE usuarios SET password_hash = ?, actualizado_en = CURRENT_TIMESTAMP WHERE id = ?";

    /** Devuelve todos los usuarios de PostgreSQL. */
    public List<UserView> findAll() {
        List<UserView> users = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(mapUser(result));
            }
            return users;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar los usuarios.", error);
        }
    }

    /** Recupera la cuenta que realmente inició sesión, no un alias fijo. */
    public UserView findById(long id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapUser(result);
                }
                throw new DatabaseException("El usuario de la sesión ya no existe.");
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo consultar el perfil de la sesión.", error);
        }
    }

    /** Devuelve el administrador activo utilizado por Mi perfil. */
    public UserView findDefaultAdministrator() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ADMIN_SQL);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return mapUser(result);
            }
            throw new DatabaseException("No existe un administrador activo.");
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo consultar el perfil.", error);
        }
    }

    /** Devuelve los roles disponibles para altas y ediciones. */
    public List<CatalogItem> findActiveRoles() {
        List<CatalogItem> roles = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(ROLES_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                roles.add(new CatalogItem(
                        result.getLong("id"),
                        result.getString("nombre"),
                        result.getString("descripcion"),
                        0
                ));
            }
            return roles;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar los roles.", error);
        }
    }

    /** Crea un usuario nuevo con contraseña cifrada opcional. */
    public void insert(
            long roleId,
            String name,
            String lastName,
            String email,
            String phone,
            String position,
            String department,
            String passwordHash
    ) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            setUserFields(statement, roleId, name, lastName, email, phone, position, department);
            statement.setString(8, emptyToNull(passwordHash));
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException(
                    "No se pudo crear el usuario. Verifica que el correo no esté repetido.",
                    error
            );
        }
    }

    /** Modifica la información general de un usuario existente. */
    public void update(
            long id,
            long roleId,
            String name,
            String lastName,
            String email,
            String phone,
            String position,
            String department
    ) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            setUserFields(statement, roleId, name, lastName, email, phone, position, department);
            statement.setLong(8, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException(
                    "No se pudo actualizar el usuario. Verifica que el correo no esté repetido.",
                    error
            );
        }
    }

    /** Activa o desactiva un usuario sin borrarlo. */
    public void toggleActive(long id) {
        executeSingleIdUpdate(TOGGLE_SQL, id, "No se pudo cambiar el estado del usuario.");
    }

    /** Consulta el hash actual utilizado por el cambio de contraseña. */
    public String findPasswordHash(long id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(PASSWORD_SQL)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("password_hash");
                }
                throw new DatabaseException("El usuario ya no existe.");
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo comprobar la contraseña.", error);
        }
    }

    /** Guarda únicamente el nuevo hash de contraseña. */
    public void updatePassword(long id, String passwordHash) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD_SQL)) {
            statement.setString(1, passwordHash);
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo cambiar la contraseña.", error);
        }
    }

    /** Coloca los siete campos compartidos por INSERT y UPDATE. */
    private void setUserFields(
            PreparedStatement statement,
            long roleId,
            String name,
            String lastName,
            String email,
            String phone,
            String position,
            String department
    ) throws SQLException {
        statement.setLong(1, roleId);
        statement.setString(2, name);
        statement.setString(3, lastName);
        statement.setString(4, email);
        statement.setString(5, emptyToNull(phone));
        statement.setString(6, emptyToNull(position));
        statement.setString(7, emptyToNull(department));
    }

    /** Ejecuta una actualización sencilla que recibe solamente un id. */
    private void executeSingleIdUpdate(String sql, long id, String message) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException(message, error);
        }
    }

    /** Convierte la fila actual a un objeto Java inmutable. */
    private UserView mapUser(ResultSet result) throws SQLException {
        Timestamp lastAccess = result.getTimestamp("ultimo_acceso");
        return new UserView(
                result.getLong("id"),
                result.getLong("rol_id"),
                result.getString("rol"),
                result.getString("nombre"),
                result.getString("apellidos"),
                result.getString("correo"),
                result.getString("telefono"),
                result.getString("puesto"),
                result.getString("departamento"),
                result.getBoolean("activo"),
                lastAccess == null ? null : lastAccess.toLocalDateTime()
        );
    }

    /** Envía NULL a PostgreSQL cuando un dato opcional está vacío. */
    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

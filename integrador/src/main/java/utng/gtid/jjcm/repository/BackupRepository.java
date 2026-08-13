package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.BackupView;

/**
 * Repositorio de metadatos de respaldos; el archivo se maneja en BackupService.
 */
public final class BackupRepository {

    /** Consulta del historial con el nombre del creador. */
    private static final String FIND_ALL_SQL =
            "SELECT r.id, r.nombre, r.ruta, r.tipo, r.estado, r.tamano_bytes, "
            + "COALESCE(CONCAT(u.nombre, ' ', u.apellidos), 'Sistema') AS creado_por, "
            + "r.creado_en, COALESCE(r.mensaje, '') AS mensaje "
            + "FROM respaldos r LEFT JOIN usuarios u ON u.id = r.creado_por "
            + "ORDER BY r.creado_en DESC, r.id DESC";

    /** Inserción inicial antes de ejecutar pg_dump. */
    private static final String INSERT_SQL =
            "INSERT INTO respaldos (nombre, ruta, tipo, estado, creado_por) "
            + "VALUES (?, ?, ?, 'EN_PROCESO', ?)";

    /** Finalización correcta o fallida del proceso externo. */
    private static final String FINISH_SQL =
            "UPDATE respaldos SET estado = ?, tamano_bytes = ?, mensaje = ? WHERE id = ?";

    /** Eliminación de metadatos después de borrar el archivo exacto. */
    private static final String DELETE_SQL = "DELETE FROM respaldos WHERE id = ?";

    /** Devuelve todo el historial. */
    public List<BackupView> findAll() {
        List<BackupView> backups = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) backups.add(mapBackup(result));
            return backups;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo consultar el historial de respaldos.", error);
        }
    }

    /** Crea metadatos EN_PROCESO y devuelve el id generado. */
    public long insertStarted(String name, String path, String type, long administratorId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, path);
            statement.setString(3, type);
            statement.setLong(4, administratorId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new DatabaseException("PostgreSQL no devolvió el id del respaldo.");
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo iniciar el registro del respaldo.", error);
        }
    }

    /** Guarda el resultado final y tamaño del archivo. */
    public void finish(long id, String status, Long sizeBytes, String message) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FINISH_SQL)) {
            statement.setString(1, status);
            if (sizeBytes == null) statement.setNull(2, Types.BIGINT);
            else statement.setLong(2, sizeBytes);
            statement.setString(3, message);
            statement.setLong(4, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo finalizar el registro del respaldo.", error);
        }
    }

    /** Elimina únicamente la fila de metadatos indicada. */
    public void delete(long id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo eliminar el historial del respaldo.", error);
        }
    }

    /** Convierte la fila actual a BackupView. */
    private BackupView mapBackup(ResultSet result) throws SQLException {
        long rawSize = result.getLong("tamano_bytes");
        Long size = result.wasNull() ? null : rawSize;
        Timestamp createdAt = result.getTimestamp("creado_en");
        return new BackupView(
                result.getLong("id"), result.getString("nombre"), result.getString("ruta"),
                result.getString("tipo"), result.getString("estado"), size,
                result.getString("creado_por"), createdAt.toLocalDateTime(),
                result.getString("mensaje")
        );
    }
}

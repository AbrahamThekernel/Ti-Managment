package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.ServiceOrderView;

/**
 * Repositorio JDBC de órdenes de servicio.
 */
public final class ServiceOrderRepository {

    /** Consulta con equipo, solicitante y responsable resueltos. */
    private static final String FIND_ALL_SQL =
            "SELECT os.id, os.equipo_id, os.folio, os.fecha_solicitud, "
            + "e.codigo || ' - ' || e.nombre AS equipo, "
            + "CONCAT(s.nombre, ' ', s.apellidos) AS solicitante, "
            + "COALESCE(CONCAT(r.nombre, ' ', r.apellidos), 'Sin asignar') AS responsable, "
            + "os.tipo_servicio, os.descripcion, os.estado, os.prioridad "
            + "FROM ordenes_servicio os "
            + "JOIN equipos e ON e.id = os.equipo_id "
            + "JOIN usuarios s ON s.id = os.solicitante_id "
            + "LEFT JOIN usuarios r ON r.id = os.responsable_id "
            + "ORDER BY os.fecha_solicitud DESC, os.id DESC";

    /** Inserción de una orden nueva. */
    private static final String INSERT_SQL =
            "INSERT INTO ordenes_servicio "
            + "(folio, equipo_id, solicitante_id, responsable_id, tipo_servicio, "
            + "descripcion, prioridad, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'ABIERTA')";

    /** Cambio de estado y fecha de cierre calculada por PostgreSQL. */
    private static final String UPDATE_STATUS_SQL =
            "UPDATE ordenes_servicio SET estado = ?, fecha_cierre = "
            + "CASE WHEN ? IN ('CERRADA', 'CANCELADA') THEN CURRENT_TIMESTAMP ELSE NULL END "
            + "WHERE id = ?";

    /** Devuelve todas las órdenes. */
    public List<ServiceOrderView> findAll() {
        List<ServiceOrderView> orders = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                orders.add(mapOrder(result));
            }
            return orders;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar las órdenes de servicio.", error);
        }
    }

    /** Crea una orden con folio generado por el controlador. */
    public void insert(
            String folio,
            long equipmentId,
            long requesterId,
            Long responsibleId,
            String serviceType,
            String description,
            String priority
    ) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, folio);
            statement.setLong(2, equipmentId);
            statement.setLong(3, requesterId);
            if (responsibleId == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, responsibleId);
            }
            statement.setString(5, serviceType);
            statement.setString(6, description);
            statement.setString(7, priority);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo crear la orden de servicio.", error);
        }
    }

    /** Actualiza el estado de una orden. */
    public void updateStatus(long id, String status) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {
            statement.setString(1, status);
            statement.setString(2, status);
            statement.setLong(3, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo cambiar el estado de la orden.", error);
        }
    }

    /** Convierte una fila SQL en ServiceOrderView. */
    private ServiceOrderView mapOrder(ResultSet result) throws SQLException {
        Timestamp timestamp = result.getTimestamp("fecha_solicitud");
        return new ServiceOrderView(
                result.getLong("id"),
                result.getLong("equipo_id"),
                result.getString("folio"),
                timestamp.toLocalDateTime(),
                result.getString("equipo"),
                result.getString("solicitante"),
                result.getString("responsable"),
                result.getString("tipo_servicio"),
                result.getString("descripcion"),
                result.getString("estado"),
                result.getString("prioridad")
        );
    }
}

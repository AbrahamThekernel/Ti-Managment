package utng.gtid.jjcm.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.MaintenanceView;

/**
 * Repositorio JDBC de mantenimientos y sincronización del estado del equipo.
 */
public final class MaintenanceRepository {

    /** Consulta de mantenimientos con equipo y técnico resueltos. */
    private static final String FIND_ALL_SQL =
            "SELECT m.id, m.equipo_id, m.folio, m.fecha_programada, "
            + "e.codigo || ' - ' || e.nombre AS equipo, m.tipo, "
            + "COALESCE(CONCAT(u.nombre, ' ', u.apellidos), 'Sin asignar') AS tecnico, "
            + "m.estado, m.prioridad, COALESCE(m.diagnostico, '') AS diagnostico, "
            + "COALESCE(m.trabajo_realizado, '') AS trabajo_realizado, m.costo "
            + "FROM mantenimientos m JOIN equipos e ON e.id = m.equipo_id "
            + "LEFT JOIN usuarios u ON u.id = m.tecnico_id "
            + "ORDER BY m.fecha_programada DESC, m.id DESC";

    /** Inserción de un mantenimiento programado. */
    private static final String INSERT_SQL =
            "INSERT INTO mantenimientos "
            + "(folio, equipo_id, tecnico_id, tipo, fecha_programada, estado, "
            + "prioridad, diagnostico, costo) "
            + "VALUES (?, ?, ?, ?, ?, 'PROGRAMADO', ?, ?, ?)";

    /** Cambio de estado y marcas de inicio/fin. */
    private static final String UPDATE_STATUS_SQL =
            "UPDATE mantenimientos SET estado = ?, "
            + "fecha_inicio = CASE WHEN ? = 'EN_PROCESO' THEN COALESCE(fecha_inicio, CURRENT_TIMESTAMP) ELSE fecha_inicio END, "
            + "fecha_fin = CASE WHEN ? IN ('COMPLETADO', 'CANCELADO') THEN CURRENT_TIMESTAMP ELSE NULL END "
            + "WHERE id = ?";

    /** Sincroniza el estado visible del equipo relacionado. */
    private static final String UPDATE_EQUIPMENT_SQL =
            "UPDATE equipos SET estado = CASE "
            + "WHEN ? = 'EN_PROCESO' THEN 'MANTENIMIENTO' "
            + "WHEN estado <> 'BAJA' THEN 'ACTIVO' ELSE estado END, "
            + "actualizado_en = CURRENT_TIMESTAMP WHERE id = ?";

    /** Devuelve todos los mantenimientos. */
    public List<MaintenanceView> findAll() {
        List<MaintenanceView> maintenance = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                maintenance.add(mapMaintenance(result));
            }
            return maintenance;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar los mantenimientos.", error);
        }
    }

    /** Crea un mantenimiento programado. */
    public void insert(
            String folio,
            long equipmentId,
            Long technicianId,
            String type,
            LocalDate scheduledDate,
            String priority,
            String diagnosis,
            BigDecimal cost
    ) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, folio);
            statement.setLong(2, equipmentId);
            if (technicianId == null) statement.setNull(3, Types.BIGINT);
            else statement.setLong(3, technicianId);
            statement.setString(4, type);
            statement.setDate(5, Date.valueOf(scheduledDate));
            statement.setString(6, priority);
            statement.setString(7, emptyToNull(diagnosis));
            statement.setBigDecimal(8, cost);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo programar el mantenimiento.", error);
        }
    }

    /**
     * Actualiza mantenimiento y equipo en una sola transacción atómica.
     */
    public void updateStatus(long id, long equipmentId, String status) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement maintenance = connection.prepareStatement(UPDATE_STATUS_SQL);
                 PreparedStatement equipment = connection.prepareStatement(UPDATE_EQUIPMENT_SQL)) {
                maintenance.setString(1, status);
                maintenance.setString(2, status);
                maintenance.setString(3, status);
                maintenance.setLong(4, id);
                maintenance.executeUpdate();

                equipment.setString(1, status);
                equipment.setLong(2, equipmentId);
                equipment.executeUpdate();
                connection.commit();
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo cambiar el estado del mantenimiento.", error);
        }
    }

    /** Convierte una fila SQL a MaintenanceView. */
    private MaintenanceView mapMaintenance(ResultSet result) throws SQLException {
        return new MaintenanceView(
                result.getLong("id"),
                result.getLong("equipo_id"),
                result.getString("folio"),
                result.getDate("fecha_programada").toLocalDate(),
                result.getString("equipo"),
                result.getString("tipo"),
                result.getString("tecnico"),
                result.getString("estado"),
                result.getString("prioridad"),
                result.getString("diagnostico"),
                result.getString("trabajo_realizado"),
                result.getBigDecimal("costo")
        );
    }

    /** Convierte textos opcionales vacíos en NULL. */
    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.DashboardStats;

/**
 * Agrupa las consultas utilizadas por el panel de estadísticas.
 */
public final class DashboardRepository {

    /** Una sola consulta obtiene los cuatro indicadores principales. */
    private static final String STATS_SQL =
            "SELECT "
            + "(SELECT COUNT(*) FROM equipos) AS total_equipos, "
            + "(SELECT COUNT(*) FROM prestamos WHERE estado IN ('ACTIVO', 'VENCIDO')) "
            + "AS prestamos_activos, "
            + "(SELECT COUNT(*) FROM ordenes_servicio WHERE estado IN ('ABIERTA', 'EN_PROCESO')) "
            + "AS ordenes_pendientes, "
            + "(SELECT COUNT(*) FROM usuarios WHERE activo = TRUE) AS usuarios_activos";

    /**
     * Ejecuta la consulta agregada y construye el resumen del dashboard.
     */
    public DashboardStats loadStats() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(STATS_SQL);
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new DatabaseException("PostgreSQL no devolvió estadísticas.");
            }
            return new DashboardStats(
                    result.getInt("total_equipos"),
                    result.getInt("prestamos_activos"),
                    result.getInt("ordenes_pendientes"),
                    result.getInt("usuarios_activos")
            );
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron cargar las estadísticas.", error);
        }
    }
}

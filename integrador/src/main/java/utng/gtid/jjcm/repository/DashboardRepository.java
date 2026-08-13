package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.DashboardStats;
import utng.gtid.jjcm.model.MonthlyLoanStats;

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
     * Genera los últimos siete meses, incluso cuando un mes no tiene registros.
     * Las subconsultas cuentan préstamos y devoluciones reales dentro de cada mes.
     */
    private static final String MONTHLY_LOANS_SQL =
            "WITH meses AS ("
            + "SELECT generate_series("
            + "date_trunc('month', CURRENT_DATE) - INTERVAL '6 months', "
            + "date_trunc('month', CURRENT_DATE), INTERVAL '1 month') AS mes"
            + ") SELECT mes::date AS mes, "
            + "(SELECT COUNT(*) FROM prestamos p "
            + "WHERE p.fecha_prestamo >= mes::date "
            + "AND p.fecha_prestamo < (mes + INTERVAL '1 month')::date) AS prestamos, "
            + "(SELECT COUNT(*) FROM prestamos p "
            + "WHERE p.fecha_devolucion_real >= mes::date "
            + "AND p.fecha_devolucion_real < (mes + INTERVAL '1 month')::date) AS devoluciones "
            + "FROM meses ORDER BY mes";

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

    /**
     * Consulta los siete meses que se dibujarán como barras en Estadísticas.
     *
     * @return una fila por mes, ordenada del más antiguo al más reciente.
     */
    public List<MonthlyLoanStats> loadMonthlyLoanStats() {
        List<MonthlyLoanStats> monthlyStats = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(MONTHLY_LOANS_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                monthlyStats.add(new MonthlyLoanStats(
                        result.getDate("mes").toLocalDate(),
                        result.getInt("prestamos"),
                        result.getInt("devoluciones")
                ));
            }
            return monthlyStats;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo cargar la gráfica mensual.", error);
        }
    }
}

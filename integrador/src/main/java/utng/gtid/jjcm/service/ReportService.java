package utng.gtid.jjcm.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.ReportData;
import utng.gtid.jjcm.repository.CatalogRepository;

/**
 * Consulta datos reales, genera documentos PDF y registra cada descarga.
 */
public final class ReportService {

    /** Tipos que se incluyen cuando el usuario solicita Exportar todos. */
    private static final List<String> ALL_TYPES = Arrays.asList(
            "INVENTARIO", "PRESTAMOS", "ORDENES", "MANTENIMIENTOS", "USUARIOS", "AUDITORIA"
    );

    /** Consultas permitidas; no se acepta SQL escrito por el usuario. */
    private static final Map<String, String> QUERIES = createQueries();

    /** Títulos legibles usados dentro de los documentos. */
    private static final Map<String, String> TITLES = createTitles();

    /** Repositorio auxiliar para identificar al administrador actual. */
    private final CatalogRepository catalogRepository = new CatalogRepository();

    /**
     * Crea un PDF individual y guarda su ruta en reportes_generados.
     */
    public void generatePdf(String type, LocalDate start, LocalDate end, Path destination) {
        ReportData report = executeReport(type, start, end);
        PdfReportWriter.write(report, start, end, destination);
        register(type, start, end, destination);
    }

    /**
     * Crea un ZIP con un PDF por módulo y registra una sola exportación global.
     */
    public void generateZip(LocalDate start, LocalDate end, Path destination) {
        try (OutputStream output = Files.newOutputStream(destination);
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (String type : ALL_TYPES) {
                ReportData report = executeReport(type, start, end);
                byte[] pdf = PdfReportWriter.createBytes(report, start, end);
                ZipEntry entry = new ZipEntry(type.toLowerCase() + ".pdf");
                zip.putNextEntry(entry);
                zip.write(pdf);
                zip.closeEntry();
            }
            register("TODOS_PDF", start, end, destination);
        } catch (IOException error) {
            throw new DatabaseException("No se pudo crear el archivo ZIP de documentos PDF.", error);
        }
    }

    /** Devuelve el número total de archivos registrados. */
    public int countGenerated() {
        String sql = "SELECT COUNT(*) FROM reportes_generados";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo contar el historial de reportes.", error);
        }
    }

    /** Ejecuta una consulta permitida y transforma el ResultSet a ReportData. */
    private ReportData executeReport(String type, LocalDate start, LocalDate end) {
        String sql = QUERIES.get(type);
        String title = TITLES.get(type);
        if (sql == null || title == null) {
            throw new IllegalArgumentException("El tipo de reporte no es válido.");
        }

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(start));
            statement.setDate(2, Date.valueOf(end));
            try (ResultSet result = statement.executeQuery()) {
                return resultSetToReportData(title, result);
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo consultar el reporte " + type + ".", error);
        }
    }

    /** Convierte nombres de columnas y filas genéricas a datos para PDF. */
    private ReportData resultSetToReportData(String title, ResultSet result) throws SQLException {
        ResultSetMetaData metadata = result.getMetaData();
        int columnCount = metadata.getColumnCount();
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        for (int column = 1; column <= columnCount; column++) {
            columns.add(formatColumnName(metadata.getColumnLabel(column)));
        }

        while (result.next()) {
            List<String> row = new ArrayList<>();
            for (int column = 1; column <= columnCount; column++) {
                String value = result.getString(column);
                row.add(value == null ? "" : value);
            }
            rows.add(row);
        }
        return new ReportData(title, columns, rows);
    }

    /** Inserta los metadatos del archivo PDF o ZIP generado. */
    private void register(String type, LocalDate start, LocalDate end, Path destination) {
        String sql = "INSERT INTO reportes_generados "
                + "(tipo, fecha_inicio, fecha_fin, ruta, creado_por) VALUES (?, ?, ?, ?, ?)";
        long administratorId = catalogRepository.findDefaultAdministratorId();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type);
            statement.setDate(2, Date.valueOf(start));
            statement.setDate(3, Date.valueOf(end));
            statement.setString(4, destination.toAbsolutePath().toString());
            statement.setLong(5, administratorId);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException(
                    "El PDF se creó, pero no se pudo registrar su historial.",
                    error
            );
        }
    }

    /** Convierte nombres como fecha_prestamo en Fecha prestamo. */
    private String formatColumnName(String columnName) {
        String normalized = columnName.replace('_', ' ').trim().toLowerCase();
        if (normalized.isEmpty()) {
            return "Columna";
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    /** Define los títulos visibles sin exponerlos a entrada libre. */
    private static Map<String, String> createTitles() {
        Map<String, String> titles = new LinkedHashMap<>();
        titles.put("INVENTARIO", "Reporte de inventario y equipos");
        titles.put("PRESTAMOS", "Reporte de préstamos y devoluciones");
        titles.put("ORDENES", "Reporte de órdenes de servicio");
        titles.put("MANTENIMIENTOS", "Reporte de mantenimientos");
        titles.put("USUARIOS", "Reporte de usuarios");
        titles.put("AUDITORIA", "Reporte de auditoría institucional");
        return titles;
    }

    /** Construye una lista cerrada de consultas parametrizadas por fecha. */
    private static Map<String, String> createQueries() {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("INVENTARIO",
                "SELECT e.codigo, e.nombre AS equipo, c.nombre AS categoria, "
                + "COALESCE(e.modelo, '') AS modelo, COALESCE(e.numero_serie, '') AS serie, "
                + "e.estado, COALESCE(u.nombre, 'Sin asignar') AS ubicacion "
                + "FROM equipos e JOIN categorias c ON c.id = e.categoria_id "
                + "LEFT JOIN ubicaciones u ON u.id = e.ubicacion_id "
                + "WHERE e.creado_en::date BETWEEN ? AND ? ORDER BY e.codigo");
        queries.put("PRESTAMOS",
                "SELECT p.folio, p.fecha_prestamo, p.fecha_devolucion_programada, p.estado, "
                + "CONCAT(u.nombre, ' ', u.apellidos) AS usuario, pr.nombre AS producto, "
                + "d.cantidad, d.cantidad_devuelta "
                + "FROM prestamos p JOIN usuarios u ON u.id = p.usuario_id "
                + "JOIN detalle_prestamo d ON d.prestamo_id = p.id "
                + "JOIN productos pr ON pr.id = d.producto_id "
                + "WHERE p.fecha_prestamo BETWEEN ? AND ? ORDER BY p.fecha_prestamo DESC");
        queries.put("ORDENES",
                "SELECT os.folio, os.fecha_solicitud, e.codigo, e.nombre AS equipo, "
                + "os.tipo_servicio, os.prioridad, os.estado "
                + "FROM ordenes_servicio os JOIN equipos e ON e.id = os.equipo_id "
                + "WHERE os.fecha_solicitud::date BETWEEN ? AND ? ORDER BY os.fecha_solicitud DESC");
        queries.put("MANTENIMIENTOS",
                "SELECT m.folio, m.fecha_programada, e.codigo, e.nombre AS equipo, "
                + "m.tipo, m.prioridad, m.estado, m.costo "
                + "FROM mantenimientos m JOIN equipos e ON e.id = m.equipo_id "
                + "WHERE m.fecha_programada BETWEEN ? AND ? ORDER BY m.fecha_programada DESC");
        queries.put("USUARIOS",
                "SELECT u.nombre, u.apellidos, u.correo, r.nombre AS rol, "
                + "COALESCE(u.departamento, '') AS departamento, u.activo, u.creado_en "
                + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
                + "WHERE u.creado_en::date BETWEEN ? AND ? ORDER BY u.nombre, u.apellidos");
        queries.put("AUDITORIA",
                "SELECT 'Usuarios creados' AS concepto, COUNT(*)::text AS valor FROM usuarios "
                + "WHERE creado_en::date BETWEEN ? AND ? "
                + "UNION ALL SELECT 'Operaciones totales', "
                + "((SELECT COUNT(*) FROM prestamos) + (SELECT COUNT(*) FROM ordenes_servicio) "
                + "+ (SELECT COUNT(*) FROM mantenimientos))::text");
        return queries;
    }
}

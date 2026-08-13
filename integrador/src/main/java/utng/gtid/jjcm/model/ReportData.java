package utng.gtid.jjcm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Datos tabulares que serán convertidos en un documento PDF.
 *
 * <p>Esta clase separa la consulta PostgreSQL del formato del archivo. Así el
 * generador de PDF no necesita conocer JDBC ni nombres de tablas.</p>
 */
public final class ReportData {

    /** Nombre legible que aparece en el encabezado del documento. */
    private final String title;

    /** Nombres de las columnas en el mismo orden que cada fila. */
    private final List<String> columns;

    /** Registros devueltos por PostgreSQL. */
    private final List<List<String>> rows;

    /**
     * Construye una copia inmutable de los datos para que no cambien mientras
     * se escribe el archivo.
     */
    public ReportData(String title, List<String> columns, List<List<String>> rows) {
        this.title = title;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));

        List<List<String>> safeRows = new ArrayList<>();
        for (List<String> row : rows) {
            safeRows.add(Collections.unmodifiableList(new ArrayList<>(row)));
        }
        this.rows = Collections.unmodifiableList(safeRows);
    }

    /** @return título visible del reporte. */
    public String getTitle() {
        return title;
    }

    /** @return columnas inmutables del reporte. */
    public List<String> getColumns() {
        return columns;
    }

    /** @return filas inmutables del reporte. */
    public List<List<String>> getRows() {
        return rows;
    }
}

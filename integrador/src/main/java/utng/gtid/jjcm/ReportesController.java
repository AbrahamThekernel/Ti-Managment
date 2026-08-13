package utng.gtid.jjcm;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.service.ReportService;

/**
 * Controlador que genera archivos PDF y ZIP desde consultas PostgreSQL.
 */
public class ReportesController extends NavigationController {

    /** Formato visible y aceptado para las fechas del reporte. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Servicio responsable de consultas, archivos e historial. */
    private final ReportService reportService = new ReportService();

    /** Campos que delimitan el periodo incluido. */
    @FXML private TextField txtFechaInicio;
    @FXML private TextField txtFechaFin;

    /** Botones normales de selección rápida del periodo. */
    @FXML private Button btnPeriodoMes;
    @FXML private Button btnPeriodoTrimestre;
    @FXML private Button btnPeriodoAnio;

    /** Botón de ZIP y botones de cada reporte PDF. */
    @FXML private Button btnExportarTodos;
    @FXML private Button btnGenerarInventario;
    @FXML private Button btnGenerarPrestamos;
    @FXML private Button btnGenerarOrdenes;
    @FXML private Button btnGenerarMantenimientos;
    @FXML private Button btnGenerarUsuarios;
    @FXML private Button btnGenerarAuditoria;

    /** Botones de ejemplo históricos conservados en el diseño. */
    @FXML private Button btnDescargarReporte1;
    @FXML private Button btnDescargarReporte2;

    /** Total real guardado en reportes_generados. */
    @FXML private Label lblReportesGenerados;

    /** Configura periodos, generadores y contador inicial. */
    @FXML
    private void initialize() {
        btnPeriodoMes.setOnAction(event -> selectMonth());
        btnPeriodoTrimestre.setOnAction(event -> selectQuarter());
        btnPeriodoAnio.setOnAction(event -> selectYear());
        btnExportarTodos.setOnAction(event -> generateZip());
        btnGenerarInventario.setOnAction(event -> generatePdf("INVENTARIO"));
        btnGenerarPrestamos.setOnAction(event -> generatePdf("PRESTAMOS"));
        btnGenerarOrdenes.setOnAction(event -> generatePdf("ORDENES"));
        btnGenerarMantenimientos.setOnAction(event -> generatePdf("MANTENIMIENTOS"));
        btnGenerarUsuarios.setOnAction(event -> generatePdf("USUARIOS"));
        btnGenerarAuditoria.setOnAction(event -> generatePdf("AUDITORIA"));

        // Las tarjetas inferiores originales no conocen una ruta de archivo real.
        btnDescargarReporte1.setOnAction(event -> showInformation(
                "Historial", "Genera un reporte nuevo para elegir dónde guardarlo."
        ));
        btnDescargarReporte2.setOnAction(event -> showInformation(
                "Historial", "Genera un reporte nuevo para elegir dónde guardarlo."
        ));

        selectMonth();
        updateCounter();
    }

    /** Selecciona desde el primer día del mes hasta hoy. */
    private void selectMonth() {
        LocalDate today = LocalDate.now();
        setPeriod(today.withDayOfMonth(1), today);
        paintPeriodButton(btnPeriodoMes);
    }

    /** Selecciona desde el primer mes del trimestre actual hasta hoy. */
    private void selectQuarter() {
        LocalDate today = LocalDate.now();
        int firstMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
        setPeriod(LocalDate.of(today.getYear(), firstMonth, 1), today);
        paintPeriodButton(btnPeriodoTrimestre);
    }

    /** Selecciona desde el primer día del año hasta hoy. */
    private void selectYear() {
        LocalDate today = LocalDate.now();
        setPeriod(today.with(TemporalAdjusters.firstDayOfYear()), today);
        paintPeriodButton(btnPeriodoAnio);
    }

    /** Coloca fechas ISO yyyy-MM-dd en ambos TextField. */
    private void setPeriod(LocalDate start, LocalDate end) {
        txtFechaInicio.setText(start.format(DATE_FORMAT));
        txtFechaFin.setText(end.format(DATE_FORMAT));
    }

    /** Genera un PDF después de pedir al usuario una ruta de destino. */
    private void generatePdf(String type) {
        LocalDate[] period = readPeriod();
        if (period == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte " + type.toLowerCase());
        chooser.setInitialFileName(type.toLowerCase() + "_utng.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
        File destination = chooser.showSaveDialog(btnExportarTodos.getScene().getWindow());
        if (destination == null) return;

        try {
            reportService.generatePdf(type, period[0], period[1], destination.toPath());
            updateCounter();
            showInformation("Reporte generado", "El archivo se guardó correctamente.");
        } catch (DatabaseException error) {
            showError("No se pudo generar el reporte", error.getMessage());
        }
    }

    /** Genera un ZIP que contiene los seis documentos PDF disponibles. */
    private void generateZip() {
        LocalDate[] period = readPeriod();
        if (period == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar todos los reportes");
        chooser.setInitialFileName("reportes_utng.zip");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo ZIP", "*.zip"));
        File destination = chooser.showSaveDialog(btnExportarTodos.getScene().getWindow());
        if (destination == null) return;

        try {
            reportService.generateZip(period[0], period[1], destination.toPath());
            updateCounter();
            showInformation(
                    "Reportes generados",
                    "El ZIP con los seis documentos PDF se guardó correctamente."
            );
        } catch (DatabaseException error) {
            showError("No se pudieron generar los reportes", error.getMessage());
        }
    }

    /** Lee, valida y ordena el periodo introducido. */
    private LocalDate[] readPeriod() {
        try {
            LocalDate start = LocalDate.parse(txtFechaInicio.getText().trim(), DATE_FORMAT);
            LocalDate end = LocalDate.parse(txtFechaFin.getText().trim(), DATE_FORMAT);
            if (end.isBefore(start)) {
                showError("Periodo incorrecto", "La fecha final no puede ser anterior a la inicial.");
                return null;
            }
            return new LocalDate[] {start, end};
        } catch (DateTimeParseException error) {
            showError("Periodo incorrecto", "Utiliza el formato yyyy-MM-dd, por ejemplo 2026-08-11.");
            return null;
        }
    }

    /** Consulta y muestra la cantidad total de reportes guardados. */
    private void updateCounter() {
        try {
            lblReportesGenerados.setText(String.valueOf(reportService.countGenerated()));
        } catch (DatabaseException error) {
            showError("No se pudo cargar el historial", error.getMessage());
        }
    }

    /** Marca únicamente el botón del periodo seleccionado. */
    private void paintPeriodButton(Button selectedButton) {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnPeriodoMes.setStyle(btnPeriodoMes == selectedButton ? selected : normal);
        btnPeriodoTrimestre.setStyle(btnPeriodoTrimestre == selectedButton ? selected : normal);
        btnPeriodoAnio.setStyle(btnPeriodoAnio == selectedButton ? selected : normal);
    }

    /** Muestra una confirmación. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra errores de validación, consulta o escritura. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

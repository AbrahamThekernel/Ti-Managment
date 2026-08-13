package utng.gtid.jjcm;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.DashboardStats;
import utng.gtid.jjcm.model.MonthlyLoanStats;
import utng.gtid.jjcm.repository.DashboardRepository;

/**
 * Controlador del panel principal de estadísticas.
 *
 * <p>La clase hereda de {@link NavigationController} para conservar los
 * métodos de navegación del menú lateral y solamente agrega la consulta de
 * los indicadores almacenados en PostgreSQL.</p>
 */
public class EstadisticasController extends NavigationController {

    /** Formato abreviado en español usado para Ene, Feb, Mar, etcétera. */
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-MX"));

    /** Repositorio que concentra la consulta SQL de los cuatro indicadores. */
    private final DashboardRepository dashboardRepository = new DashboardRepository();

    /** Etiqueta FXML donde se muestra la cantidad de equipos registrados. */
    @FXML
    private Label lblTotalEquipos;

    /** Etiqueta FXML donde se muestra la cantidad de préstamos sin devolver. */
    @FXML
    private Label lblPrestamosActivos;

    /** Etiqueta FXML donde se muestra la cantidad de órdenes pendientes. */
    @FXML
    private Label lblOrdenesPendientes;

    /** Etiqueta FXML donde se muestra la cantidad de usuarios habilitados. */
    @FXML
    private Label lblUsuariosActivos;

    /** Gráfica de barras declarada directamente en estadisticas.fxml. */
    @FXML
    private BarChart<String, Number> graficaPrestamos;

    /** Botón visual que muestra el año actual de los datos. */
    @FXML
    private Button btnSeleccionarAnio;

    /**
     * FXMLLoader ejecuta este método automáticamente al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        loadStatistics();
        loadMonthlyChart();
    }

    /** Consulta PostgreSQL y construye las series de barras. */
    private void loadMonthlyChart() {
        try {
            // Cada serie produce una barra distinta dentro del mismo mes.
            XYChart.Series<String, Number> loansSeries = new XYChart.Series<>();
            loansSeries.setName("Préstamos");

            XYChart.Series<String, Number> returnsSeries = new XYChart.Series<>();
            returnsSeries.setName("Devoluciones");

            for (MonthlyLoanStats month : dashboardRepository.loadMonthlyLoanStats()) {
                String monthName = capitalize(month.getMonth().format(MONTH_FORMAT));

                // Los tonos se aplican en línea para conservar el proyecto sin CSS externo.
                loansSeries.getData().add(
                        createBar(monthName, month.getLoans(), "#666666")
                );
                returnsSeries.getData().add(
                        createBar(monthName, month.getReturns(), "#b5b5b5")
                );
            }

            // Se limpian datos anteriores antes de agregar las dos series actuales.
            graficaPrestamos.getData().clear();
            graficaPrestamos.getData().add(loansSeries);
            graficaPrestamos.getData().add(returnsSeries);
            btnSeleccionarAnio.setText(String.valueOf(LocalDate.now().getYear()));
        } catch (DatabaseException error) {
            showDatabaseError(error.getMessage());
        }
    }

    /** Crea una barra y espera a que JavaFX construya su nodo para darle tono. */
    private XYChart.Data<String, Number> createBar(String month, int amount, String color) {
        XYChart.Data<String, Number> bar = new XYChart.Data<>(month, amount);
        bar.nodeProperty().addListener((observable, previousNode, currentNode) ->
                applyBarColor(currentNode, color)
        );
        return bar;
    }

    /** Aplica el tono únicamente cuando JavaFX ya creó el rectángulo. */
    private void applyBarColor(Node node, String color) {
        if (node != null) {
            node.setStyle("-fx-bar-fill: " + color + ";");
        }
    }

    /** Convierte "ene" en "Ene" sin mantener meses escritos a mano. */
    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Consulta PostgreSQL y coloca cada resultado en su etiqueta.
     */
    private void loadStatistics() {
        try {
            DashboardStats stats = dashboardRepository.loadStats();
            // forLanguageTag evita el constructor obsoleto de Locale en JDK recientes.
            NumberFormat format = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-MX"));

            lblTotalEquipos.setText(format.format(stats.getTotalEquipment()));
            lblPrestamosActivos.setText(format.format(stats.getActiveLoans()));
            lblOrdenesPendientes.setText(format.format(stats.getPendingOrders()));
            lblUsuariosActivos.setText(format.format(stats.getActiveUsers()));
        } catch (DatabaseException error) {
            showDatabaseError(error.getMessage());
        }
    }

    /**
     * Informa el problema sin cerrar de forma inesperada toda la aplicación.
     */
    private void showDatabaseError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de base de datos");
        alert.setHeaderText("No fue posible cargar las estadísticas");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package utng.gtid.jjcm;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.DashboardStats;
import utng.gtid.jjcm.repository.DashboardRepository;

/**
 * Controlador del panel principal de estadísticas.
 *
 * <p>La clase hereda de {@link NavigationController} para conservar los
 * métodos de navegación del menú lateral y solamente agrega la consulta de
 * los indicadores almacenados en PostgreSQL.</p>
 */
public class EstadisticasController extends NavigationController {

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

    /**
     * FXMLLoader ejecuta este método automáticamente al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        loadStatistics();
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

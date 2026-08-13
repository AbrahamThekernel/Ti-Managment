package utng.gtid.jjcm;

import java.io.IOException;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;
import utng.gtid.jjcm.service.LoanService;

/**
 * Controlador del formulario para registrar un préstamo.
 *
 * Extiende NavigationController para reutilizar la navegación del menú lateral
 * sin repetir todos sus métodos.
 */
public class NuevoPrestamoController extends NavigationController {

    /** Servicio que aplica reglas y ejecuta transacciones de PostgreSQL. */
    private final LoanService loanService = new LoanService();

    /** Lista desplegable de usuarios obtenidos de la tabla usuarios. */
    @FXML
    private ComboBox<CatalogItem> cmbProfesor;

    /** Lista desplegable de productos con stock mayor que cero. */
    @FXML
    private ComboBox<CatalogItem> cmbEquipo;

    /** Campo numérico de unidades solicitadas. */
    @FXML
    private TextField txtCantidad;

    /** Campo opcional que explica el uso del producto. */
    @FXML
    private TextField txtMotivo;

    /** Fecha de entrega registrada en prestamos.fecha_prestamo. */
    @FXML
    private DatePicker dpFechaPrestamo;

    /** Fecha límite registrada en prestamos.fecha_devolucion_programada. */
    @FXML
    private DatePicker dpFechaDevolucion;

    /** Notas libres que se almacenan con el préstamo. */
    @FXML
    private TextArea txtObservaciones;

    /** Confirmación obligatoria de responsabilidad sobre el producto. */
    @FXML
    private CheckBox chkResponsiva;

    /** Nombre seleccionado mostrado en el panel derecho. */
    @FXML
    private Label lblProfesorResumen;

    /** Producto seleccionado mostrado en el panel derecho. */
    @FXML
    private Label lblEquipoResumen;

    /** Stock real consultado desde PostgreSQL. */
    @FXML
    private Label lblDisponibilidad;

    /** Mensaje de error o confirmación visible para el usuario. */
    @FXML
    private Label lblMensaje;

    /** Botón que se deshabilita después de un registro exitoso. */
    @FXML
    private Button btnRegistrarPrestamo;

    /**
     * JavaFX ejecuta este método automáticamente al terminar de cargar el FXML.
     */
    @FXML
    private void initialize() {
        // El formulario propone la fecha actual y una devolución tres días después.
        dpFechaPrestamo.setValue(LocalDate.now());
        dpFechaDevolucion.setValue(LocalDate.now().plusDays(3));
        txtCantidad.setText("1");

        // Los ComboBox se llenan con registros reales y conservan sus llaves primarias.
        try {
            cmbProfesor.setItems(FXCollections.observableArrayList(
                    loanService.findBorrowers()
            ));
            cmbEquipo.setItems(FXCollections.observableArrayList(
                    loanService.findAvailableProducts()
            ));
        } catch (DatabaseException error) {
            mostrarError(error.getMessage());
            btnRegistrarPrestamo.setDisable(true);
        }

        // Actualizamos el resumen cuando cambia el usuario seleccionado.
        cmbProfesor.valueProperty().addListener((observable, anterior, nuevo) -> {
            lblProfesorResumen.setText(nuevo == null ? "Sin seleccionar" : nuevo.getName());
        });

        // Mostramos producto y stock obtenidos de la base de datos.
        cmbEquipo.valueProperty().addListener((observable, anterior, nuevo) -> {
            lblEquipoResumen.setText(nuevo == null ? "Sin seleccionar" : nuevo.getName());
            lblDisponibilidad.setText(
                    nuevo == null
                            ? "Selecciona un equipo"
                            : nuevo.getAvailable() + " unidades disponibles"
            );
        });
    }

    /**
     * Valida los datos capturados antes de registrar el préstamo.
     */
    @FXML
    private void registrarPrestamo() {
        lblMensaje.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException error) {
            mostrarError("Escribe una cantidad numérica válida.");
            return;
        }

        try {
            // El servicio valida y el repositorio guarda préstamo, detalle y stock.
            String folio = loanService.register(
                    cmbProfesor.getValue(),
                    cmbEquipo.getValue(),
                    cantidad,
                    dpFechaPrestamo.getValue(),
                    dpFechaDevolucion.getValue(),
                    txtMotivo.getText(),
                    txtObservaciones.getText(),
                    chkResponsiva.isSelected()
            );

            lblMensaje.setText("Préstamo registrado correctamente. Folio: " + folio);
            lblMensaje.setStyle("-fx-text-fill: #222222; -fx-font-weight: bold;");
            btnRegistrarPrestamo.setDisable(true);
        } catch (DatabaseException error) {
            mostrarError(error.getMessage());
        }
    }

    /** Regresa al listado sin registrar cambios. */
    @FXML
    private void cancelar() {
        try {
            App.setRoot("prestamos");
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo regresar a Préstamos.", error);
        }
    }

    /** Muestra un mensaje de validación dentro del formulario. */
    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-text-fill: #555555; -fx-font-weight: bold;");
    }

}

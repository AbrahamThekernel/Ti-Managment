package utng.gtid2.inicio;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controlador para CrearOrden.fxml
 * Esta misma pantalla se reutiliza para EDITAR una orden existente:
 * si OrdenServicioDAO.getOrdenSeleccionada() no es null, el formulario
 * se precarga con esos datos y "Guardar" actualiza en lugar de crear.
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/CrearOrdenController.java
 *
 * fx:id requeridos en el FXML:
 *   lblTitulo, cmbCliente, cmbTipoServicio, cmbTecnico, dpFecha,
 *   cmbPrioridad, txtDireccion, txtDescripcion, txtMateriales
 *
 * Botones (onAction): onCancelar, onLimpiar, onCrearOrden
 */
public class CrearOrdenController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cmbCliente;
    @FXML private ComboBox<String> cmbTipoServicio;
    @FXML private ComboBox<String> cmbTecnico;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbPrioridad;
    @FXML private TextField txtDireccion;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtMateriales;

    // Si no es null, estamos editando esta orden en lugar de crear una nueva
    private OrdenServicio ordenEnEdicion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCliente.setItems(FXCollections.observableArrayList(
                "Grupo Alfa", "Constructora Beta", "Casa López",
                "Inmobiliaria Norte", "Hotel Paraíso"));

        cmbTipoServicio.setItems(FXCollections.observableArrayList(
                "Eléctrico", "Plomería", "Acabados", "Estructura"));

        cmbTecnico.setItems(FXCollections.observableArrayList(
                "Sin asignar", "Luis Ramos", "Ana Torres", "Mario Díaz"));

        cmbPrioridad.setItems(FXCollections.observableArrayList(
                "Normal", "Alta", "Urgente"));
        cmbPrioridad.setValue("Normal");

        // ¿Venimos de "Editar" en ListaOrdenes o DetalleOrden?
        ordenEnEdicion = OrdenServicioDAO.getOrdenSeleccionada();
        if (ordenEnEdicion != null) {
            precargarDatos(ordenEnEdicion);
        }
    }

    /** Llena el formulario con los datos de la orden a editar. */
    private void precargarDatos(OrdenServicio o) {
        lblTitulo.setText("Editar orden de servicio - " + o.getNumero());
        cmbCliente.setValue(o.getCliente());
        cmbTipoServicio.setValue(o.getTipoServicio());
        cmbTecnico.setValue(o.getTecnico());
        cmbPrioridad.setValue(o.getPrioridad());
        txtDireccion.setText(o.getDireccion());
        txtDescripcion.setText(o.getDescripcion());
        txtMateriales.setText(o.getMateriales());
        try {
            dpFecha.setValue(LocalDate.parse(o.getFecha()));
        } catch (Exception ignored) {
            // Si la fecha guardada no tiene formato ISO, se deja el DatePicker vacío
        }
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onCrearOrden(ActionEvent event) {
        if (!camposValidos()) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor completa todos los campos obligatorios antes de guardar.");
            return;
        }

        if (ordenEnEdicion != null) {
            // --- Modo edición: actualizamos la orden existente ---
            OrdenServicio actualizada = new OrdenServicio(
                    ordenEnEdicion.getNumero(),
                    cmbCliente.getValue(),
                    txtDescripcion.getText(),
                    cmbTecnico.getValue(),
                    dpFecha.getValue() != null ? dpFecha.getValue().toString() : "",
                    ordenEnEdicion.getEstado(),
                    cmbTipoServicio.getValue(),
                    cmbPrioridad.getValue(),
                    txtDireccion.getText(),
                    txtMateriales.getText()
            );
            OrdenServicioDAO.actualizar(ordenEnEdicion, actualizada);
            mostrarAlerta(AlertType.INFORMATION, "Orden actualizada",
                    "Los cambios se guardaron correctamente.");
        } else {
            // --- Modo creación: nueva orden ---
            OrdenServicio nueva = new OrdenServicio(
                    OrdenServicioDAO.generarNuevoNumero(),
                    cmbCliente.getValue(),
                    txtDescripcion.getText(),
                    cmbTecnico.getValue() != null ? cmbTecnico.getValue() : "Sin asignar",
                    dpFecha.getValue() != null ? dpFecha.getValue().toString() : "",
                    "Pendiente",
                    cmbTipoServicio.getValue(),
                    cmbPrioridad.getValue(),
                    txtDireccion.getText(),
                    txtMateriales.getText()
            );
            OrdenServicioDAO.agregar(nueva);
            mostrarAlerta(AlertType.INFORMATION, "Orden creada",
                    "La orden de servicio se registró correctamente.");
        }

        OrdenServicioDAO.setOrdenSeleccionada(null);
        Navegacion.navegarA("ListaOrdenes.fxml");
    }

    @FXML
    private void onLimpiar(ActionEvent event) {
        cmbCliente.setValue(null);
        cmbTipoServicio.setValue(null);
        cmbTecnico.setValue(null);
        cmbPrioridad.setValue("Normal");
        dpFecha.setValue(null);
        txtDireccion.clear();
        txtDescripcion.clear();
        txtMateriales.clear();
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        OrdenServicioDAO.setOrdenSeleccionada(null);
        Navegacion.navegarA("ListaOrdenes.fxml");
    }

    // ----------------------------------------------------------------
    // Validación y utilidades
    // ----------------------------------------------------------------

    private boolean camposValidos() {
        return cmbCliente.getValue() != null
                && cmbTipoServicio.getValue() != null
                && txtDireccion.getText() != null && !txtDireccion.getText().isBlank()
                && txtDescripcion.getText() != null && !txtDescripcion.getText().isBlank();
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

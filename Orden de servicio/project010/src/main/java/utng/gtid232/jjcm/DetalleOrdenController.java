package utng.gtid232.jjcm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para DetalleOrden.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/DetalleOrdenController.java
 *
 * fx:id requeridos en el FXML:
 *   lblNumero, lblCliente, lblTipo, lblTecnico, lblFecha, lblEstado,
 *   lblPrioridad, lblDireccion, lblDescripcion,
 *   tablaMateriales, colMatNombre, colMatCantidad, colMatCosto,
 *   listBitacora, txtNota
 *
 * Botones (onAction): onVolver, onEditar, onEliminar, onMarcarCompletada, onAgregarNota
 */
public class DetalleOrdenController implements Initializable {

    @FXML private Label lblNumero;
    @FXML private Label lblCliente;
    @FXML private Label lblTipo;
    @FXML private Label lblTecnico;
    @FXML private Label lblFecha;
    @FXML private Label lblEstado;
    @FXML private Label lblPrioridad;
    @FXML private Label lblDireccion;
    @FXML private Label lblDescripcion;

    @FXML private TableView<MaterialUsado> tablaMateriales;
    @FXML private TableColumn<MaterialUsado, String> colMatNombre;
    @FXML private TableColumn<MaterialUsado, String> colMatCantidad;
    @FXML private TableColumn<MaterialUsado, String> colMatCosto;

    @FXML private ListView<String> listBitacora;
    @FXML private TextField txtNota;

    private OrdenServicio orden;
    private final ObservableList<String> bitacora = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colMatNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMatCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colMatCosto.setCellValueFactory(new PropertyValueFactory<>("costo"));

        orden = OrdenServicioDAO.getOrdenSeleccionada();

        if (orden == null) {
            // Protección ante NullPointerException si se entra a esta pantalla
            // sin haber seleccionado una orden previamente.
            mostrarAlerta(AlertType.WARNING, "Sin orden seleccionada",
                    "No se seleccionó ninguna orden. Regresando a la lista.");
            Navegacion.navegarA("ListaOrdenes.fxml");
            return;
        }

        cargarDatosOrden();
        cargarMaterialesDemo();
        cargarBitacoraDemo();
    }

    private void cargarDatosOrden() {
        lblNumero.setText(orden.getNumero());
        lblCliente.setText(orden.getCliente());
        lblTipo.setText(orden.getTipoServicio());
        lblTecnico.setText(orden.getTecnico());
        lblFecha.setText(orden.getFecha());
        lblEstado.setText(orden.getEstado());
        lblPrioridad.setText(orden.getPrioridad());
        lblDireccion.setText(orden.getDireccion());
        lblDescripcion.setText(orden.getDescripcion());
    }

    /** Datos de ejemplo; en una integración real vendrían de una tabla relacionada. */
    private void cargarMaterialesDemo() {
        ObservableList<MaterialUsado> materiales = FXCollections.observableArrayList();
        if (orden.getMateriales() != null && !orden.getMateriales().isBlank()) {
            for (String item : orden.getMateriales().split(",")) {
                materiales.add(new MaterialUsado(item.trim(), "1", "$0.00"));
            }
        }
        tablaMateriales.setItems(materiales);
    }

    private void cargarBitacoraDemo() {
        bitacora.clear();
        bitacora.add("Orden creada — " + orden.getFecha());
        bitacora.add("Estado actual: " + orden.getEstado());
        listBitacora.setItems(bitacora);
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onVolver(ActionEvent event) {
        OrdenServicioDAO.setOrdenSeleccionada(null);
        Navegacion.navegarA("ListaOrdenes.fxml");
    }

    @FXML
    private void onEditar(ActionEvent event) {
        // La orden ya está marcada como "seleccionada" en el DAO,
        // CrearOrdenController detectará esto y precargará el formulario.
        Navegacion.navegarA("CrearOrden.fxml");
    }

    @FXML
    private void onEliminar(ActionEvent event) {
        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar orden");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar definitivamente la orden " + orden.getNumero() + "?");

        confirmacion.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.OK) {
                OrdenServicioDAO.eliminar(orden);
                OrdenServicioDAO.setOrdenSeleccionada(null);
                mostrarAlerta(AlertType.INFORMATION, "Orden eliminada",
                        "La orden se eliminó correctamente.");
                Navegacion.navegarA("ListaOrdenes.fxml");
            }
        });
    }

    @FXML
    private void onMarcarCompletada(ActionEvent event) {
        OrdenServicio actualizada = new OrdenServicio(
                orden.getNumero(), orden.getCliente(), orden.getDescripcion(),
                orden.getTecnico(), orden.getFecha(), "Completada",
                orden.getTipoServicio(), orden.getPrioridad(),
                orden.getDireccion(), orden.getMateriales());

        OrdenServicioDAO.actualizar(orden, actualizada);
        orden = actualizada;
        lblEstado.setText("Completada");
        bitacora.add("Orden marcada como completada.");

        mostrarAlerta(AlertType.INFORMATION, "Orden actualizada",
                "La orden se marcó como completada.");
    }

    @FXML
    private void onAgregarNota(ActionEvent event) {
        String nota = txtNota.getText();
        if (nota == null || nota.isBlank()) {
            mostrarAlerta(AlertType.WARNING, "Nota vacía",
                    "Escribe una nota antes de agregarla a la bitácora.");
            return;
        }
        bitacora.add(nota.trim());
        txtNota.clear();
    }

    // ----------------------------------------------------------------
    // Utilidad
    // ----------------------------------------------------------------

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /** Modelo interno simple para la tabla de materiales usados en esta orden. */
    public static class MaterialUsado {
        private final javafx.beans.property.SimpleStringProperty nombre;
        private final javafx.beans.property.SimpleStringProperty cantidad;
        private final javafx.beans.property.SimpleStringProperty costo;

        public MaterialUsado(String nombre, String cantidad, String costo) {
            this.nombre = new javafx.beans.property.SimpleStringProperty(nombre);
            this.cantidad = new javafx.beans.property.SimpleStringProperty(cantidad);
            this.costo = new javafx.beans.property.SimpleStringProperty(costo);
        }

        public String getNombre() { return nombre.get(); }
        public String getCantidad() { return cantidad.get(); }
        public String getCosto() { return costo.get(); }
    }
}

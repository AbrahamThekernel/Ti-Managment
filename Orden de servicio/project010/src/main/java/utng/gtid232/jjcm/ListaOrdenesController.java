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
 * Controlador para ListaOrdenes.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/ListaOrdenesController.java
 *
 * fx:id requeridos en el FXML:
 *   txtBuscar, cmbFiltroEstado, tablaOrdenes,
 *   colNumero, colCliente, colDescripcion, colTecnico, colFecha, colEstado
 *
 * Botones (onAction):
 *   onBuscar, onActualizar, onCrearOrden, onDetalle, onEditar, onEliminar
 */
public class ListaOrdenesController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<OrdenServicio> tablaOrdenes;

    @FXML private TableColumn<OrdenServicio, String> colNumero;
    @FXML private TableColumn<OrdenServicio, String> colCliente;
    @FXML private TableColumn<OrdenServicio, String> colDescripcion;
    @FXML private TableColumn<OrdenServicio, String> colTecnico;
    @FXML private TableColumn<OrdenServicio, String> colFecha;
    @FXML private TableColumn<OrdenServicio, String> colEstado;

    // Lista completa (sin filtrar) para poder restaurar tras una búsqueda
    private ObservableList<OrdenServicio> listaCompleta;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- Vincula cada columna con la propiedad correspondiente del modelo ---
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colTecnico.setCellValueFactory(new PropertyValueFactory<>("tecnico"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // --- Combo de filtro por estado ---
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Pendiente", "En proceso", "Completada", "Pausada"));
        cmbFiltroEstado.setValue("Todos");

        cargarDatos();
    }

    /** Carga (o recarga) los datos del repositorio en memoria a la tabla. */
    private void cargarDatos() {
        listaCompleta = OrdenServicioDAO.obtenerTodas();
        tablaOrdenes.setItems(listaCompleta);
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onBuscar(ActionEvent event) {
        String filtroTexto = txtBuscar.getText() == null
                ? "" : txtBuscar.getText().trim().toLowerCase();
        String filtroEstado = cmbFiltroEstado.getValue();

        ObservableList<OrdenServicio> resultado = FXCollections.observableArrayList();
        for (OrdenServicio o : OrdenServicioDAO.obtenerTodas()) {
            boolean coincideTexto = filtroTexto.isEmpty()
                    || o.getNumero().toLowerCase().contains(filtroTexto)
                    || o.getCliente().toLowerCase().contains(filtroTexto)
                    || o.getDescripcion().toLowerCase().contains(filtroTexto);

            boolean coincideEstado = filtroEstado == null
                    || filtroEstado.equals("Todos")
                    || o.getEstado().equalsIgnoreCase(filtroEstado);

            if (coincideTexto && coincideEstado) {
                resultado.add(o);
            }
        }
        tablaOrdenes.setItems(resultado);
    }

    @FXML
    private void onActualizar(ActionEvent event) {
        txtBuscar.clear();
        cmbFiltroEstado.setValue("Todos");
        cargarDatos();
    }

    @FXML
    private void onCrearOrden(ActionEvent event) {
        // Aseguramos que no quede ninguna orden "seleccionada" de una edición previa
        OrdenServicioDAO.setOrdenSeleccionada(null);
        Navegacion.navegarA("CrearOrden.fxml");
    }

    @FXML
    private void onDetalle(ActionEvent event) {
        OrdenServicio seleccionada = tablaOrdenes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(AlertType.WARNING, "Sin selección",
                    "Selecciona una orden de la tabla para ver su detalle.");
            return;
        }
        OrdenServicioDAO.setOrdenSeleccionada(seleccionada);
        Navegacion.navegarA("DetalleOrden.fxml");
    }

    @FXML
    private void onEditar(ActionEvent event) {
        OrdenServicio seleccionada = tablaOrdenes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(AlertType.WARNING, "Sin selección",
                    "Selecciona una orden de la tabla para editarla.");
            return;
        }
        OrdenServicioDAO.setOrdenSeleccionada(seleccionada);
        Navegacion.navegarA("CrearOrden.fxml");
    }

    @FXML
    private void onEliminar(ActionEvent event) {
        OrdenServicio seleccionada = tablaOrdenes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(AlertType.WARNING, "Sin selección",
                    "Selecciona una orden de la tabla para eliminarla.");
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar orden");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar la orden " + seleccionada.getNumero() + "?");

        confirmacion.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.OK) {
                OrdenServicioDAO.eliminar(seleccionada);
                cargarDatos();
                mostrarAlerta(AlertType.INFORMATION, "Orden eliminada",
                        "La orden se eliminó correctamente.");
            }
        });
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
}

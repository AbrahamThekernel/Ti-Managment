package utng.gtid232.inicio;

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
 * Controlador para ListaInventario.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/ListaInventarioController.java
 *
 * fx:id requeridos en el FXML:
 *   txtBuscar, tablaInventario,
 *   colId, colCodigo, colNombre, colCategoria, colStock, colEstado, colFecha
 *
 * Botones (onAction): onBuscar, onActualizar, onAgregar, onDetalle, onEditar, onEliminar
 */
public class ListaInventarioController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Material> tablaInventario;

    @FXML private TableColumn<Material, String> colId;
    @FXML private TableColumn<Material, String> colCodigo;
    @FXML private TableColumn<Material, String> colNombre;
    @FXML private TableColumn<Material, String> colCategoria;
    @FXML private TableColumn<Material, String> colStock;
    @FXML private TableColumn<Material, String> colEstado;
    @FXML private TableColumn<Material, String> colFecha;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- Vincula cada columna con la propiedad correspondiente del modelo ---
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoStock"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));

        cargarDatos();
    }

    /** Carga (o recarga) los datos del repositorio en memoria a la tabla. */
    private void cargarDatos() {
        tablaInventario.setItems(MaterialDAO.obtenerTodos());
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onBuscar(ActionEvent event) {
        String filtro = txtBuscar.getText() == null
                ? "" : txtBuscar.getText().trim().toLowerCase();

        if (filtro.isEmpty()) {
            cargarDatos();
            return;
        }

        ObservableList<Material> resultado = FXCollections.observableArrayList();
        for (Material m : MaterialDAO.obtenerTodos()) {
            boolean coincide = m.getCodigo().toLowerCase().contains(filtro)
                    || m.getNombre().toLowerCase().contains(filtro)
                    || m.getCategoria().toLowerCase().contains(filtro);
            if (coincide) {
                resultado.add(m);
            }
        }
        tablaInventario.setItems(resultado);
    }

    @FXML
    private void onActualizar(ActionEvent event) {
        txtBuscar.clear();
        cargarDatos();
    }

    @FXML
    private void onAgregar(ActionEvent event) {
        MaterialDAO.setMaterialSeleccionado(null);
        Navegacion.navegarA("AgregarMaterial.fxml");
    }

    @FXML
    private void onDetalle(ActionEvent event) {
        Material seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(AlertType.WARNING, "Sin selección",
                    "Selecciona un material de la tabla para ver su detalle.");
            return;
        }
        MaterialDAO.setMaterialSeleccionado(seleccionado);
        Navegacion.navegarA("DetalleMaterial.fxml");
    }

    @FXML
    private void onEditar(ActionEvent event) {
        Material seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(AlertType.WARNING, "Sin selección",
                    "Selecciona un material de la tabla para editarlo.");
            return;
        }
        MaterialDAO.setMaterialSeleccionado(seleccionado);
        Navegacion.navegarA("EditarMaterial.fxml");
    }

    @FXML
    private void onEliminar(ActionEvent event) {
        Material seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(AlertType.WARNING, "Sin selección",
                    "Selecciona un material de la tabla para eliminarlo.");
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar material");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar el material " + seleccionado.getCodigo() + "?");

        confirmacion.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.OK) {
                MaterialDAO.eliminar(seleccionado);
                cargarDatos();
                mostrarAlerta(AlertType.INFORMATION, "Material eliminado",
                        "El material se eliminó correctamente.");
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

package utng.gtid2.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para DetalleMaterial.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/DetalleMaterialController.java
 *
 * fx:id requeridos en el FXML:
 *   lblCodigo, lblNombre, lblCategoria, lblUnidad, lblStock, lblStockMin,
 *   lblPrecio, lblProveedor, lblEstado, lblFecha, lblDescripcion
 *
 * Botones (onAction): onVolver, onEditar
 */
public class DetalleMaterialController implements Initializable {

    @FXML private Label lblCodigo;
    @FXML private Label lblNombre;
    @FXML private Label lblCategoria;
    @FXML private Label lblUnidad;
    @FXML private Label lblStock;
    @FXML private Label lblStockMin;
    @FXML private Label lblPrecio;
    @FXML private Label lblProveedor;
    @FXML private Label lblEstado;
    @FXML private Label lblFecha;
    @FXML private Label lblDescripcion;

    private Material material;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        material = MaterialDAO.getMaterialSeleccionado();

        if (material == null) {
            // Protección ante NullPointerException si se entra sin seleccionar material
            mostrarAlerta(AlertType.WARNING, "Sin material seleccionado",
                    "No se seleccionó ningún material. Regresando al inventario.");
            Navegacion.navegarA("ListaInventario.fxml");
            return;
        }

        cargarDatos();
    }

    private void cargarDatos() {
        lblCodigo.setText(material.getCodigo());
        lblNombre.setText(material.getNombre());
        lblCategoria.setText(material.getCategoria());
        lblUnidad.setText(material.getUnidadMedida());
        lblStock.setText(material.getCantidad() + " " + material.getUnidadMedida());
        lblStockMin.setText(material.getStockMinimo() + " " + material.getUnidadMedida());
        lblPrecio.setText("$" + material.getPrecio() + " MXN");
        lblProveedor.setText(material.getProveedor());
        lblEstado.setText(material.getEstadoStock());
        lblFecha.setText(material.getFechaRegistro());
        lblDescripcion.setText(material.getDescripcion());
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onVolver(ActionEvent event) {
        MaterialDAO.setMaterialSeleccionado(null);
        Navegacion.navegarA("ListaInventario.fxml");
    }

    @FXML
    private void onEditar(ActionEvent event) {
        // El material ya está marcado como "seleccionado" en el DAO,
        // EditarMaterialController detectará esto y precargará el formulario.
        Navegacion.navegarA("EditarMaterial.fxml");
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

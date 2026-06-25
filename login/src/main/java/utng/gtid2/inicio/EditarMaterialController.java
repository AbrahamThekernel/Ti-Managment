package utng.gtid232.jjcm;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para EditarMaterial.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/EditarMaterialController.java
 *
 * fx:id requeridos en el FXML:
 *   txtCodigo, txtNombre, cmbCategoria, cmbUnidad, txtStock, txtStockMin,
 *   txtPrecio, txtProveedor, txtDescripcion
 *
 * Botones (onAction): onCancelar, onRestablecer, onGuardar
 */
public class EditarMaterialController implements Initializable {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMin;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtProveedor;
    @FXML private TextArea txtDescripcion;

    // Material que se está editando (referencia original, sin modificar)
    private Material materialOriginal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "Eléctrico", "Plomería", "Acabados", "Estructura", "Construcción"));

        cmbUnidad.setItems(FXCollections.observableArrayList(
                "Piezas", "Metros", "Litros", "Sacos", "Cubetas", "Cajas"));

        materialOriginal = MaterialDAO.getMaterialSeleccionado();

        if (materialOriginal == null) {
            // Protección ante NullPointerException si se entra sin seleccionar material
            mostrarAlerta(AlertType.WARNING, "Sin material seleccionado",
                    "No se seleccionó ningún material. Regresando al inventario.");
            Navegacion.navegarA("ListaInventario.fxml");
            return;
        }

        precargarDatos(materialOriginal);
    }

    private void precargarDatos(Material m) {
        txtCodigo.setText(m.getCodigo());
        txtNombre.setText(m.getNombre());
        cmbCategoria.setValue(m.getCategoria());
        cmbUnidad.setValue(m.getUnidadMedida());
        txtStock.setText(m.getCantidad());
        txtStockMin.setText(m.getStockMinimo());
        txtPrecio.setText(m.getPrecio());
        txtProveedor.setText(m.getProveedor());
        txtDescripcion.setText(m.getDescripcion());
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onGuardar(ActionEvent event) {
        if (materialOriginal == null) return;

        if (!camposValidos()) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor completa todos los campos obligatorios antes de guardar.");
            return;
        }

        if (!sonNumericos()) {
            mostrarAlerta(AlertType.WARNING, "Datos inválidos",
                    "Stock, stock mínimo y precio deben ser valores numéricos.");
            return;
        }

        String estado = Material.calcularEstado(txtStock.getText().trim(), txtStockMin.getText().trim());

        Material actualizado = new Material(
                materialOriginal.getId(),
                materialOriginal.getCodigo(), // el código no es editable en el FXML
                txtNombre.getText().trim(),
                cmbCategoria.getValue(),
                txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "",
                txtStock.getText().trim(),
                txtStockMin.getText().trim(),
                cmbUnidad.getValue(),
                txtPrecio.getText().trim(),
                txtProveedor.getText().trim(),
                materialOriginal.getFechaRegistro(),
                estado
        );

        MaterialDAO.actualizar(materialOriginal, actualizado);

        mostrarAlerta(AlertType.INFORMATION, "Material actualizado",
                "Los cambios se guardaron correctamente.");

        MaterialDAO.setMaterialSeleccionado(null);
        Navegacion.navegarA("ListaInventario.fxml");
    }

    @FXML
    private void onRestablecer(ActionEvent event) {
        if (materialOriginal != null) {
            precargarDatos(materialOriginal);
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        MaterialDAO.setMaterialSeleccionado(null);
        Navegacion.navegarA("ListaInventario.fxml");
    }

    // ----------------------------------------------------------------
    // Validación y utilidades
    // ----------------------------------------------------------------

    private boolean camposValidos() {
        return notBlank(txtNombre.getText())
                && cmbCategoria.getValue() != null
                && cmbUnidad.getValue() != null
                && notBlank(txtStock.getText())
                && notBlank(txtStockMin.getText())
                && notBlank(txtPrecio.getText())
                && notBlank(txtProveedor.getText());
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private boolean sonNumericos() {
        try {
            Double.parseDouble(txtStock.getText().trim());
            Double.parseDouble(txtStockMin.getText().trim());
            Double.parseDouble(txtPrecio.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

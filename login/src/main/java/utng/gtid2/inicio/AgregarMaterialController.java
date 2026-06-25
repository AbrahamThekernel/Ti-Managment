package utng.gtid232.jjcm;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador para AgregarMaterial.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/AgregarMaterialController.java
 *
 * fx:id requeridos en el FXML:
 *   txtCodigo, txtNombre, cmbCategoria, cmbUnidad, txtStock, txtStockMin,
 *   txtPrecio, txtProveedor, txtDescripcion
 *
 * Botones (onAction): onCancelar, onLimpiar, onGuardar
 */
public class AgregarMaterialController implements Initializable {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMin;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtProveedor;
    @FXML private TextArea txtDescripcion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "Eléctrico", "Plomería", "Acabados", "Estructura", "Construcción"));

        cmbUnidad.setItems(FXCollections.observableArrayList(
                "Piezas", "Metros", "Litros", "Sacos", "Cubetas", "Cajas"));

        // Código sugerido automáticamente (el usuario puede modificarlo)
        txtCodigo.setText(MaterialDAO.generarNuevoCodigo());
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onGuardar(ActionEvent event) {
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

        Material nuevo = new Material(
                MaterialDAO.generarNuevoId(),
                txtCodigo.getText().trim(),
                txtNombre.getText().trim(),
                cmbCategoria.getValue(),
                txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "",
                txtStock.getText().trim(),
                txtStockMin.getText().trim(),
                cmbUnidad.getValue(),
                txtPrecio.getText().trim(),
                txtProveedor.getText().trim(),
                fechaHoyFormateada(),
                estado
        );

        MaterialDAO.agregar(nuevo);

        mostrarAlerta(AlertType.INFORMATION, "Material registrado",
                "El material se agregó correctamente al inventario.");

        Navegacion.navegarA("ListaInventario.fxml");
    }

    @FXML
    private void onLimpiar(ActionEvent event) {
        txtCodigo.setText(MaterialDAO.generarNuevoCodigo());
        txtNombre.clear();
        cmbCategoria.setValue(null);
        cmbUnidad.setValue(null);
        txtStock.clear();
        txtStockMin.clear();
        txtPrecio.clear();
        txtProveedor.clear();
        txtDescripcion.clear();
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        Navegacion.navegarA("ListaInventario.fxml");
    }

    // ----------------------------------------------------------------
    // Validación y utilidades
    // ----------------------------------------------------------------

    private boolean camposValidos() {
        return notBlank(txtCodigo.getText())
                && notBlank(txtNombre.getText())
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

    private String fechaHoyFormateada() {
        LocalDate hoy = LocalDate.now();
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        return String.format("%02d %s %d", hoy.getDayOfMonth(), mes, hoy.getYear());
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

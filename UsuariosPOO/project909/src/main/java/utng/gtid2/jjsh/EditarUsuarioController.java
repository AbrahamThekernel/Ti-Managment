package utng.gtid2.jjsh;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EditarUsuarioController implements Initializable {

    @FXML private TextField txtIdUsua;
    @FXML private TextField txtNombreC;
    @FXML private TextField txtNomUsua;
    @FXML private ChoiceBox<String> cmbRol;
    @FXML private ChoiceBox<String> cmbEstado;
    @FXML private ChoiceBox<String> cmbArea;
    @FXML private DatePicker dpFechaR;
    @FXML private PasswordField pwdContraseña;
    @FXML private Button btnBuscar;
    @FXML private Button btnCancelar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRegresar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (cmbRol != null) cmbRol.setItems(FXCollections.observableArrayList("Administrador", "Encargado", "Soporte Técnico"));
        if (cmbEstado != null) cmbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        if (cmbArea != null) cmbArea.setItems(FXCollections.observableArrayList("Soporte Técnico UTNG", "Sistemas", "Laboratorios"));
    }

    @FXML
    void handleBtnBuscar(ActionEvent event) {
        String id = txtIdUsua.getText().trim();
        if (id.isEmpty()) {
            System.out.println("[ALERTA] Ingrese un ID.");
            return;
        }
        txtNombreC.setText("Anastacio Rodríguez García");
        txtNomUsua.setText("Anastxd1167");
        cmbRol.setValue("Administrador");
        cmbEstado.setValue("Activo");
        cmbArea.setValue("Soporte Técnico UTNG");
        dpFechaR.setValue(LocalDate.now());
        pwdContraseña.setText("123456");
    }

    @FXML
    void handleBtnCancelar(ActionEvent event) {
        if (txtIdUsua != null) txtIdUsua.clear();
        if (txtNombreC != null) txtNombreC.clear();
        if (txtNomUsua != null) txtNomUsua.clear();
        if (pwdContraseña != null) pwdContraseña.clear();
        if (dpFechaR != null) dpFechaR.setValue(null);
        if (cmbRol != null) cmbRol.setValue(null);
        if (cmbEstado != null) cmbEstado.setValue(null);
        if (cmbArea != null) cmbArea.setValue(null);
    }

    @FXML
    void handleBtnActualizar(ActionEvent event) {
        System.out.println("[ÉXITO] Usuario " + txtIdUsua.getText() + " actualizado.");
    }

    @FXML
    void handleBtnEliminar(ActionEvent event) {
        System.out.println("[ÉXITO] Usuario " + txtIdUsua.getText() + " eliminado.");
        handleBtnCancelar(null);
    }

    @FXML
    void handleBtnRegresar(ActionEvent event) {
        try { App.setRoot("menuUsuarios"); } catch (IOException e) { e.printStackTrace(); }
    }
}
package utng.gtid2.inicio;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RolesCrearController {

    @FXML
    private Button btnRegresar;

    @FXML
    private TextField txtNombreRol;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private CheckBox chkVerUsuarios;

    @FXML
    private CheckBox chkEditarUsuarios;

    @FXML
    private CheckBox chkEliminarUsuarios;

    @FXML
    private CheckBox chkVerReportes;

    @FXML
    private CheckBox chkEditarConfiguracion;

    @FXML
    private CheckBox chkGestionarRoles;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardarRol;

    @FXML
    public void initialize() {

        btnCancelar.setDisable(false);
        btnGuardarRol.setDisable(false);

    }

    @FXML
    private void regresar() throws IOException {
        App.setRoot("ConfiguracionRoles");
    }

    @FXML
    private void cancelar() throws IOException {
        App.setRoot("ConfiguracionRoles");
    }

    @FXML
    private void guardarRol() throws IOException {
        App.setRoot("Confirmacion");
    }
}
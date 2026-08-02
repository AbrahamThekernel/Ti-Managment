package utng.gtid2.inicio;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;

public class ConfiguracionSeguridadController {

    @FXML
    private Button btnRegresar;

    @FXML
    private PasswordField txtContrasenaVieja;

    @FXML
    private PasswordField txtContrasenaNueva;

    @FXML
    private CheckBox chkRequiereMayusculas;

    @FXML
    private CheckBox chkRequiereSimbolos;

    @FXML
    private CheckBox chkRequiereNumeros;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardarCambios;

    

    @FXML
    public void initialize() {

        btnRegresar.setDisable(false);
        btnCancelar.setDisable(false);
        btnGuardarCambios.setDisable(false);

    }

    @FXML
    private void regresar() throws IOException {
        App.setRoot("Configuracion");
    }

    @FXML
    private void cancelar() throws IOException {
        App.setRoot("Configuracion");
    }

    @FXML
    private void guardarCambios() throws IOException {
        App.setRoot("Confirmacion");
    }
}
package utng.gtid2.inicio;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ConfiguracionController {

    @FXML
    private Button btnGuardarCambios;

    @FXML
    private Button btnGuardarCambios1;

    @FXML
    private Button btnEditarGeneral;

    @FXML
    private Button btnGestionarRoles;

    @FXML
    private Button btnEditarSeguridad;

    @FXML
    public void initialize() {

        btnGuardarCambios.setDisable(false);
        btnGuardarCambios1.setDisable(false);
        btnEditarGeneral.setDisable(false);
        btnGestionarRoles.setDisable(false);
        btnEditarSeguridad.setDisable(false);

    }

    @FXML
    private void regresar() throws IOException {
        App.setRoot("Configuracion");
    }

    @FXML
private void abrirGuardar() throws IOException {
    App.setRoot("Confirmacion");
}
    @FXML
    private void abrirEditar1() throws IOException {
        App.setRoot("ConfiguracionGeneral");
    }

    @FXML
    private void abrirRoles() throws IOException {
        App.setRoot("ConfiguracionRoles");
    }

    @FXML
    private void abrirEditar() throws IOException {
        App.setRoot("ConfiguracionSeguridad");
    }
}
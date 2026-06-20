package utng.gtid2.cmjam;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ConfiguracionGeneralController {

    @FXML
    private Button btnRegresar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardarCambios;

    @FXML
    private TextField txtNombreEmpresa;

    @FXML
    private ComboBox<String> cmbIdioma;

    @FXML
    private ComboBox<String> cmbZonaHoraria;

    @FXML
    private ComboBox<String> cmbFormatoFecha;

    @FXML
    public void initialize() {

        cmbIdioma.getItems().addAll("Español", "Inglés", "Portugués");
        cmbZonaHoraria.getItems().addAll("GMT-6 (Ciudad de México)", "GMT-5 (Bogotá)", "GMT-3 (Buenos Aires)");
        cmbFormatoFecha.getItems().addAll("DD/MM/AAAA", "MM/DD/AAAA", "AAAA-MM-DD");

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
    private void activarGuardar() throws IOException {
        App.setRoot("Confirmacion");
    }
}
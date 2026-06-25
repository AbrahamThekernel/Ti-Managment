package utng.gtid2.inicio;

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
import javafx.scene.control.TextField;

public class ConsultarUsuarioController implements Initializable {

    @FXML private TextField txtIdUsuar;
    @FXML private TextField txtCombreComp;
    @FXML private TextField txtNomUsu;
    @FXML private ChoiceBox<String> cmbRol;
    @FXML private ChoiceBox<String> cmbEstado;
    @FXML private ChoiceBox<String> cmbArea;
    @FXML private DatePicker dpFechRe;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnRegrsar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (cmbRol != null) cmbRol.setItems(FXCollections.observableArrayList("Administrador", "Encargado", "Soporte Técnico"));
        if (cmbEstado != null) cmbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        if (cmbArea != null) cmbArea.setItems(FXCollections.observableArrayList("Soporte Técnico UTNG", "Sistemas", "Laboratorios"));
    }

    @FXML
    void handleBtnBuscar(ActionEvent event) {
        String id = txtIdUsuar.getText().trim();
        if (id.isEmpty()) {
            System.out.println("[ALERTA] Ingrese un ID.");
            return;
        }
        txtCombreComp.setText("Anastacio Rodríguez García");
        txtNomUsu.setText("Anastxd1167");
        cmbRol.setValue("Administrador");
        cmbEstado.setValue("Activo");
        cmbArea.setValue("Soporte Técnico UTNG");
        dpFechRe.setValue(LocalDate.now());
    }

    @FXML
    void handleBtnLimpiar(ActionEvent event) {
        if (txtIdUsuar != null) txtIdUsuar.clear();
        if (txtCombreComp != null) txtCombreComp.clear();
        if (txtNomUsu != null) txtNomUsu.clear();
        if (dpFechRe != null) dpFechRe.setValue(null);
        if (cmbRol != null) cmbRol.setValue(null);
        if (cmbEstado != null) cmbEstado.setValue(null);
        if (cmbArea != null) cmbArea.setValue(null);
    }

    @FXML
    void handleBtnRegrsar(ActionEvent event) {
        try { App.setRoot("menuUsuarios"); } catch (IOException e) { e.printStackTrace(); }
    }
}
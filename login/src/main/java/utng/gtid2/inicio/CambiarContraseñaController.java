package utng.gtid2.inicio;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class CambiarContraseñaController {

    @FXML private TextField txtIdUsuario;
    @FXML private TextField txtNuevaC;
    @FXML private TextField txtConfirC;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    @FXML
    void handleBtnGuardar(ActionEvent event) {
        String id = txtIdUsuario.getText().trim();
        String pass = txtNuevaC.getText();
        String confirm = txtConfirC.getText();

        if (id.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            System.out.println("[ALERTA] Faltan campos obligatorios.");
            return;
        }
        if (!pass.equals(confirm)) {
            System.out.println("[ERROR] Las contraseñas no coinciden.");
            return;
        }
        System.out.println("[ÉXITO] Cambio guardado.");
    }

    @FXML
    void handleBtnCancelar(ActionEvent event) {
        try { App.setRoot("menuUsuarios"); } catch (IOException e) { e.printStackTrace(); }
    }
}
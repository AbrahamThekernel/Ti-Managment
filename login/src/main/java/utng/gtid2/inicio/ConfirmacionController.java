package utng.gtid2.inicio;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ConfirmacionController {


    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnConfirmar;



    @FXML
    private void cancelar() throws IOException {
        App.setRoot("Configuracion");
    }

    @FXML
    private void confirmar() throws IOException {
        App.setRoot("Configuracion");
    }

}

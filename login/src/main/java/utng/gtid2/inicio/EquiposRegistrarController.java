package utng.gtid2.inicio;


import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EquiposRegistrarController {

    @FXML
    private Button btnRegresar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardarEquipo;

    @FXML
private void regresar() throws IOException {
    App.setRoot("Equipos");
}

    @FXML
    private void cancelar() throws IOException {
        App.setRoot("EquiposRegistrar");
    }

    @FXML
    private void abrirConfirmacion() throws IOException {
        App.setRoot("Confirmacion");
    }


   


}
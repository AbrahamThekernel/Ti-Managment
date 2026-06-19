package utng.gtid2.cmjam;



import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EquiposBajaController {

    @FXML
    private Button btnRegresar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnConfirmarBaja;

    @FXML
private void regresar() throws IOException {
    App.setRoot("Equipos");
}

    @FXML
    private void cancelar() throws IOException {
        App.setRoot("Equipos");
    }

    @FXML
private void abrirConfirmacion() {
    try {
        App.setRoot("Confirmacion");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}

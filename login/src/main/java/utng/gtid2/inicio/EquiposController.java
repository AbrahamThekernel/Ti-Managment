package utng.gtid2.inicio;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EquiposController {
    @FXML
    private Button btnRegistrarEquipo; 

    @FXML
    private Button btnEditarEquipo;

    @FXML
    private Button btnDarBaja;

     @FXML
    private Button btnVerHistorial;


    @FXML
public void initialize() {

    btnEditarEquipo.setDisable(false);
    btnDarBaja.setDisable(false);
    btnVerHistorial.setDisable(false);

}



    @FXML
    private void abrirRegistrar() throws IOException {
        App.setRoot("EquiposRegistrar");
    }

    @FXML
private void abrirEditarEquipo() throws IOException {
    App.setRoot("EquiposEditar");
}
     @FXML
    private void abrirBaja() throws IOException {
        App.setRoot("EquiposBaja");
    }
        @FXML
    private void abrirVerHistorial() throws IOException {
        App.setRoot("EquiposHistorial");
}
}
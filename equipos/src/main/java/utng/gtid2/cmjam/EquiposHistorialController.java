package utng.gtid2.cmjam;


import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EquiposHistorialController {

    @FXML
    private Button btnRegresar;
   
    @FXML
private void regresar() throws IOException {
    App.setRoot("Equipos");
}

   


}
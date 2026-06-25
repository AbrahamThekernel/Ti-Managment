package utng.gtid2.inicio;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EstadisticasInventarioController {
    @FXML
    private Button btnRegresar; 
    


   @FXML
private void regresar() throws IOException {
    App.setRoot("Estadisticas");
}
}
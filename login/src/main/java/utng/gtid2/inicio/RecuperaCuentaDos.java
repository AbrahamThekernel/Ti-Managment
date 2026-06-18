package utng.gtid2.inicio;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class RecuperaCuentaDos {
    @FXML
    private Button btnRegresar;

    @FXML
    private Button btnSalir;

    @FXML
    private void regresar(){
        try {
        App.setRoot("login");
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
    @FXML 
    private void salir(){
        System.exit(0);

    }
}

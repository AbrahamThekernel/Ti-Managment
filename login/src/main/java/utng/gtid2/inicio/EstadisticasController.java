package utng.gtid2.inicio;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EstadisticasController {
    @FXML
    private Button btnActualizar; 
    @FXML
    private Button btnRegresar;
    @FXML
    private Button btnActualizar1;
    @FXML
    private Button btnVerTodasNotificaciones; 
    @FXML
    private Button btnVerInventario;
    @FXML
    private Button btnVerTodasNotificaciones1;



    @FXML
    private void abrirNotificacion() throws IOException {
        App.setRoot("EstadisticasNotificaciones");
    }

    @FXML
private void abrirInventario() throws IOException {
    App.setRoot("EstadisticasInventario");
}
     @FXML
    private void abrirHistorial() throws IOException {
        App.setRoot("EstadisticaHistorial");
    }
}



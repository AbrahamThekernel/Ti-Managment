package utng.gtid2.inicio;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EstadisticasNotificacionesController {

    @FXML
    private Button btnRegresar;
    @FXML
    private Button btnMarcarTodasLeidas;

    @FXML
private void regresar() throws IOException {
    App.setRoot("Estadisticas");
}

    @FXML
    private void marcarTodasLeidas() {
        // Aquí puedes agregar la lógica para marcar todas las notificaciones como leídas
        System.out.println("Todas las notificaciones han sido marcadas como leídas.");
    }


}


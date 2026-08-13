package utng.gtid2.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;




public class CerrarSesionController {

    @FXML
    private void onCancelar(ActionEvent event) {
        App.navigateTo("MenuPerfil.fxml");
    }

    @FXML
    private void onConfirmarCerrarSesion(ActionEvent event) {
        System.exit(0);
    }
}

package utng.gtid2.inicio.Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class ConfirmacionController {

    @FXML
    private void handleConfirmar(ActionEvent event) {

        System.out.println("Formulario enviado");

        cerrarVentana(event);
    }

    @FXML
    private void handleCancelar(ActionEvent event) {

        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {

        Node source = (Node) event.getSource();

        Stage stage = (Stage) source.getScene().getWindow();

        stage.close();
    }
}
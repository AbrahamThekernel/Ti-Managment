package utng.gtid2.inicio;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RecuperaCuenta {

    @FXML
    private TextField txtContraseña;

    @FXML
    private TextField txtCorreo;

    @FXML
    private Button btnRegresar;

    @FXML
    private Button btnEnviar;


    @FXML
    private void handleEnviar(ActionEvent event) {

        // Validar correo
        if (txtCorreo.getText().trim().isEmpty()) {

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("El correo es obligatorio.");
            alerta.showAndWait();

            return;
        }

        // Validar contraseña
        if (txtContraseña.getText().trim().isEmpty()) {

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("La contraseña es obligatoria.");
            alerta.showAndWait();

            return;
        }

        // Si los datos son válidos, abrir modal
        abrirModal(event);
    }


    private void abrirModal(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/utng/gtid2/inicio/confirmar.fxml"
                )
            );

            Parent root = loader.load();

            Stage modal = new Stage();

            modal.setTitle("Confirmación");

            modal.setScene(new Scene(root));

            modal.initModality(Modality.WINDOW_MODAL);

            Stage ventanaPrincipal =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            modal.initOwner(ventanaPrincipal);

            modal.showAndWait();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }


    @FXML
    private void regresar() {

        try {

            App.setRoot("login");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
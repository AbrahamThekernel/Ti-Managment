package utng.gtid2.inicio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class RecuperaCuenta {
    @FXML
    private TextField txtRecuperarUsuario;

    @FXML
    private TextField txtRecuperarCorreo;

    @FXML
    private Button btnAceptar;

    @FXML
    private Button btnRegresar;

//*Cosorro mi cerebro ya no puede con mas */

    @FXML
    private void regresar(){
         try {
            App.setRoot("login");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    private void aceptar(){
        
    if (txtRecuperarUsuario.getText().trim().isEmpty()) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText("El usuario es obligatorio.");
        alerta.showAndWait();
        return;
    }

    if (txtRecuperarCorreo.getText().trim().isEmpty()) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText("El correo es obligatorio.");
        alerta.showAndWait();
        return;
    }

    try {
        App.setRoot("recupera_cuenta_dos");
    } catch (Exception e) {
        e.printStackTrace();
}
    }
    }



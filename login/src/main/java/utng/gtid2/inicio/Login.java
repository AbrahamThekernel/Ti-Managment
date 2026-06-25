package utng.gtid2.inicio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login {
    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtContraseña;
    @FXML
    private Button btnAceptar;
    @FXML
    private Button btnSalir;
    @FXML
    private Button btnReestablecer;

    @FXML
    private void aceptar(){
        
        if (txtUsuario.getText().trim().isEmpty()) {
            
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText("El usuario es obligatorio.");
        alerta.showAndWait();
        return;
    }

    if (txtContraseña.getText().trim().isEmpty()) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText("La contraseña es obligatoria.");
        alerta.showAndWait();
        return;
    }

        try {
            App.setRoot("MenuPrincipal");
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    @FXML
    private void salir(){
        System.exit(0);
        
    }
    @FXML
    private void reestablecer(){
        try {
            App.setRoot("recuperar_cuenta");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

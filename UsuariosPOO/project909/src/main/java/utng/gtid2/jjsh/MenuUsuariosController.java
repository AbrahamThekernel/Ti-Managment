package utng.gtid2.jjsh;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MenuUsuariosController {

    @FXML private Button btnConsultarU;
    @FXML private Button btnEditarU;
    @FXML private Button btnCambiarC;
    @FXML private Button btnRegresar;

    @FXML
    void handleBtnConsultarU(ActionEvent event) {
        navigate("consultarUsuario");
    }

    @FXML
    void handleBtnEditarU(ActionEvent event) {
        navigate("EditarUsuario");
    }

    @FXML
    void handleBtnCambiarC(ActionEvent event) {
        navigate("CambiarContraseña");
    }

    @FXML
    void handleBtnRegresar(ActionEvent event) {
        navigate("primary");
    }

    private void navigate(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            System.err.println("Error abriendo: " + fxml);
            e.printStackTrace();
        }
    }
}
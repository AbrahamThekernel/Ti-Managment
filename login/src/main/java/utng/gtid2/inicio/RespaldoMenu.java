package utng.gtid2.inicio;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class RespaldoMenu {
    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnProgramarNuevo;
    @FXML
    private Button btnRestaurar;
    @FXML
    private Button btnHistorial;

    @FXML
    private void nuevo(){
        try {
            App.setRoot("respaldo_Nuevo");
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }

    @FXML
    private void historial(){
        try {
            App.setRoot("respaldo_Historial");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void programar(){
        try {
            App.setRoot("respaldo_programar");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @FXML
    private void restaurar(){
        try {
            App.setRoot("respaldo_restaurar");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
}

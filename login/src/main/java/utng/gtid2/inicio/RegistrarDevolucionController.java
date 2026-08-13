package utng.gtid2.inicio;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class RegistrarDevolucionController {

    @FXML private TextField txtIdPrestamo;
    @FXML private TextField txtEquipo;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private ChoiceBox<String> cmbEstadoEquipo;
    @FXML private TextArea txtAObservaciones;

    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;

    @FXML
    public void handleBtnGuardar() throws IOException {
        System.out.println("Guardando devolución... Regresando al menú.");
        App.setRoot("Menu_Prestamos");
    }

    
    @FXML
    private void handleBtnCancelar() throws IOException {
        System.out.println("Cancelando devolución... Regresando al menú.");
        App.setRoot("Menu_Prestamos");
    }
}
package utng.gtid2.inicio;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class RegistrarPrestamoController {

    // Componentes FXML que coinciden exactamente con tus fx:id de la vista
    @FXML private TextField txtIdPrestamo;
    @FXML private ChoiceBox<String> cmbCliente;
    @FXML private ChoiceBox<String> cmbEquipo;
    @FXML private TextField txtNumeroSerie;
    @FXML private DatePicker dpFechaPrestamo;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private TextArea txtAObservaciones;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    @FXML
    public void handleBtnGuardar() {
        // Aquí se programará la inserción a la base de datos más adelante
        System.out.println("Guardando préstamo... Regresando al menú.");
        regresarAlMenu();
    }

    @FXML
    public void handleBtnCancelar() {
        System.out.println("Cancelando préstamo... Regresando al menú.");
        regresarAlMenu();
    }

    private void regresarAlMenu() {
        try {
            App.setRoot("Menu_Prestamos");
        } catch (IOException e) {
            System.out.println("Error al regresar al menú principal.");
            e.printStackTrace();
        }
    }
}
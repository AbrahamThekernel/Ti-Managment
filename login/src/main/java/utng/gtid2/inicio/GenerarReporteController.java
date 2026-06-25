package utng.gtid2.inicio;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import utng.gtid2.jjsh.App;

public class GenerarReporteController {

    @FXML private ChoiceBox<String> cmbTipoReporte;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private Button btnCancelar;
    @FXML private Button btnGenerar;

    @FXML
    public void initialize() {
        
        if (cmbTipoReporte != null) {
            cmbTipoReporte.getItems().addAll("Préstamos Activos", "Devoluciones Tardías", "Inventario General");
        }
    }

    @FXML
    public void handleBtnGenerar() {
        System.out.println("Generando reporte en PDF... (Lógica de exportación aquí)");
    }

    @FXML
    public void handleBtnCancelar() {
        System.out.println("Cancelando... Regresando al menú.");
        regresarAlMenu();
    }

    private void regresarAlMenu() {
        try {
            utng.gtid2.jjsh.App.setRoot("Menu_Prestamos");
        } catch (IOException e) {
            System.out.println("Error al regresar al menú principal.");
            e.printStackTrace();
        }
    }
}
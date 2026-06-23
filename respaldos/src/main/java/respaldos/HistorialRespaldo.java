package respaldos;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class HistorialRespaldo {

    @FXML
    private ComboBox<String> cmbRespaldos;

    @FXML
    public void initialize() {

        cmbRespaldos.getItems().addAll(
            "respaldo_2026_06_20.sql",
            "respaldo_2026_06_21.sql",
            "respaldo_2026_06_22.sql"
        );
    }
}
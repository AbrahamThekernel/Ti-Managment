package utng.gtid.jjcm;

import java.io.IOException;
import javafx.fxml.FXML;

/**
 * Controlador conservado únicamente para la vista secundaria generada por la
 * plantilla original de Maven. Las pantallas institucionales no dependen de él.
 */
public class SecondaryController {

    /** Regresa a la pantalla primary.fxml cuando se abre esta vista de plantilla. */
    @FXML
    private void switchToPrimary() throws IOException {
        // App.setRoot sustituye el contenido sin abrir una ventana adicional.
        App.setRoot("primary");
    }
}

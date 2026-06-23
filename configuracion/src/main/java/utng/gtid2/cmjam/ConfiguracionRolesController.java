package utng.gtid2.cmjam;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ConfiguracionRolesController {

    @FXML
    private Button btnRegresar;

    @FXML
    private Button btnNuevoRol;

    @FXML
    private TableView<?> tablaRoles;

    @FXML
    private TableColumn<?, ?> colRol;

    @FXML
    private TableColumn<?, ?> colDescripcion;

    @FXML
    private TableColumn<?, ?> colUsuariosAsignados;

    @FXML
    private TableColumn<?, ?> colPermisos;

    @FXML
    private Label lblRolSeleccionado;

    @FXML
    private Button btnEditarRol;

    @FXML
    private Button btnEliminarRol;

    @FXML
    public void initialize() {

        btnRegresar.setDisable(false);
        btnNuevoRol.setDisable(false);

    }

    @FXML
    private void regresar() throws IOException {
        App.setRoot("Configuracion");
    }

    @FXML
    private void abrirNuevoRol() throws IOException {
        App.setRoot("RolesCrear");
    }
}
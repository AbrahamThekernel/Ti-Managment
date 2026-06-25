package utng.gtid2.cmjam;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MenuPrincipalController {

    // Botones del sidebar
    @FXML private Button btnEstadisticas;
    @FXML private Button btnInventario;
    @FXML private Button btnPrestamos;
    @FXML private Button btnOrdenes;
    @FXML private Button btnMantenimientos;
    @FXML private Button btnEquipos;
    @FXML private Button btnUsuarios;
    @FXML private Button btnReportes;
    @FXML private Button btnRespaldos;
    @FXML private Button btnConfiguracion;
    @FXML private Button btnPerfil;
    @FXML private Button btnCerrarSesion;

    // Label de usuario en la barra superior
    @FXML private Label lblUsuario;

    // Paneles del contenido central
    @FXML private VBox panelEstadisticas;
    @FXML private VBox panelInventario;
    @FXML private VBox panelPrestamos;
    @FXML private VBox panelOrdenes;
    @FXML private VBox panelMantenimientos;
    @FXML private VBox panelEquipos;
    @FXML private VBox panelUsuarios;
    @FXML private VBox panelReportes;
    @FXML private VBox panelRespaldos;
    @FXML private VBox panelConfiguracion;
    @FXML private VBox panelPerfil;

    @FXML
    public void initialize() {
        btnEstadisticas.setDisable(false);
        btnInventario.setDisable(false);
        btnPrestamos.setDisable(false);
        btnOrdenes.setDisable(false);
        btnMantenimientos.setDisable(false);
        btnEquipos.setDisable(false);
        btnUsuarios.setDisable(false);
        btnReportes.setDisable(false);
        btnRespaldos.setDisable(false);
        btnConfiguracion.setDisable(false);
        btnPerfil.setDisable(false);
        btnCerrarSesion.setDisable(false);
    }

    // ── Métodos de navegación entre paneles ──

    @FXML
    private void mostrarEstadisticas() throws IOException {
        App.setRoot("Estadisticas");
    }

    @FXML
    private void mostrarInventario() throws IOException {
        App.setRoot("ListaInventario");
    }

    @FXML
    private void mostrarPrestamos() throws IOException {
        App.setRoot("Menu_Prestamos");
    }

    @FXML
    private void mostrarOrdenes() throws IOException {
        App.setRoot("ListaOrdenes");
    }

    @FXML
    private void mostrarMantenimientos() throws IOException {
        App.setRoot("Equipos");
    }

    @FXML
    private void mostrarEquipos() throws IOException {
        App.setRoot("Equipos");
    }

    @FXML
    private void mostrarUsuarios() throws IOException {
        App.setRoot("menuUsuarios");
    }

    @FXML
    private void mostrarReportes() throws IOException {
        App.setRoot("GenerarReporte");
    }

    @FXML
    private void mostrarRespaldos() throws IOException {
        App.setRoot("respaldoMenu");
    }

    @FXML
    private void mostrarConfiguracion() throws IOException {
        App.setRoot("Configuracion");
    }

    @FXML
    private void mostrarPerfil() throws IOException {
        App.setRoot("MenuPerfil");
    }

    @FXML
    private void cerrarSesion() throws IOException {
        App.setRoot("login");
    }
}
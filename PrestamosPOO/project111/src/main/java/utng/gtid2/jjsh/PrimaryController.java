package utng.gtid2.jjsh;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PrimaryController {

    @FXML private Button btnRegistrarPrestamo;
    @FXML private Button btnRegistrarDevolucion;
    @FXML private Button btnEditarPrestamo;
    @FXML private Button btnConsultarPrestamos;
    @FXML private Button btnGenerarReporte;
    @FXML private Button btnSalir;

    // 1. Ir a Registrar Préstamo
    @FXML
    public void handleBtnRegistrarPrestamo() {
        try {
            App.setRoot("RegistrarPrestamo");
        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de Registrar Préstamo.");
            e.printStackTrace();
        }
    }

    // 2. Ir a Registrar Devolución
    @FXML
    public void handleBtnRegistrarDevolucion() {
        try {
            App.setRoot("RegistrarDevolucion");
        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de Registrar Devolución.");
            e.printStackTrace();
        }
    }

    // 3. Ir a Editar Préstamo
    @FXML
    public void handleBtnEditarPrestamo() {
        try {
            App.setRoot("EditarPrestamo");
        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de Editar Préstamo.");
            e.printStackTrace();
        }
    }

    // 4. Ir a Consultar Préstamos
    @FXML
    public void handleBtnConsultarPrestamos() {
        try {
            App.setRoot("ConsultarPrestamo");
        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de Consultar Préstamos.");
            e.printStackTrace();
        }
    }


// 5. Generar Reporte
   @FXML
public void handleBtnGenerarReporte() {
    try {
        utng.gtid2.jjsh.App.setRoot("GenerarReporte");
    } catch (IOException e) {
        System.out.println("Error al abrir la pantalla de reportes.");
        e.printStackTrace();
    }
}

    // 5. Cerrar la aplicación por completo
    @FXML
    public void handleBtnSalir() {
        System.exit(0);
    }
}
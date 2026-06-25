package utng.gtid2.inicio;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import utng.gtid2.jjsh.App;

public class ConsultarPrestamoController {

    // Componentes de búsqueda y tabla
    @FXML private TextField txtBuscarPrestamo;
    @FXML private TableView<?> tblPrestamos;
    @FXML private TableColumn<?, ?> colIdPrestamo;
    @FXML private TableColumn<?, ?> colCliente;
    @FXML private TableColumn<?, ?> colEquipo;
    @FXML private TableColumn<?, ?> colNumeroSerie;
    @FXML private TableColumn<?, ?> colFechaPrestamo;
    @FXML private TableColumn<?, ?> colFechaDevolucion;
    @FXML private TableColumn<?, ?> colEstado;

    // Botones de acción
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRegresar;

    @FXML
    public void handleBtnBuscar() {
        System.out.println("Buscando préstamo...");
    }

    @FXML
    public void handleBtnLimpiar() {
        System.out.println("Limpiando filtro de búsqueda...");
        txtBuscarPrestamo.clear();
    }

    @FXML
    public void handleBtnEditar() {
        System.out.println("Abriendo edición del préstamo seleccionado...");
        // Más adelante aquí puedes hacer que brinque a la pantalla de Editar
    }

    @FXML
    public void handleBtnEliminar() {
        System.out.println("Eliminando préstamo seleccionado...");
    }

    @FXML
    public void handleBtnRegresar() {
        try {
            App.setRoot("Menu_Prestamos");
        } catch (IOException e) {
            System.out.println("Error al regresar al menú principal.");
            e.printStackTrace();
        }
    }
}
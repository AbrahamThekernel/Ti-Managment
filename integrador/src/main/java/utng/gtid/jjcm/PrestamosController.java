package utng.gtid.jjcm;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;
import utng.gtid.jjcm.model.LoanView;
import utng.gtid.jjcm.service.LoanService;

/**
 * Controlador de la pantalla que consulta préstamos y registra devoluciones.
 */
public class PrestamosController extends NavigationController {

    /** Formato utilizado por las fechas visibles en la tabla. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT);

    /** Servicio con reglas y transacciones de préstamos. */
    private final LoanService loanService = new LoanService();

    /** Indicador superior de préstamos pendientes. */
    @FXML
    private Label lblPrestamosActivos;

    /** Indicador superior de préstamos finalizados. */
    @FXML
    private Label lblPrestamosDevueltos;

    /** Primera fila de tarjetas de productos. */
    @FXML
    private HBox filaProductos1;

    /** Segunda fila de tarjetas de productos. */
    @FXML
    private HBox filaProductos2;

    /** Contenedor donde se generan las filas consultadas de PostgreSQL. */
    @FXML
    private VBox contenedorPrestamos;

    /** Filtro que muestra todos los estados. */
    @FXML
    private Button btnFiltroTodos;

    /** Filtro que muestra activos y vencidos. */
    @FXML
    private Button btnFiltroActivos;

    /** Filtro que muestra solamente los devueltos. */
    @FXML
    private Button btnFiltroDevueltos;

    /**
     * Configura eventos y carga datos cuando FXMLLoader crea la pantalla.
     */
    @FXML
    private void initialize() {
        btnFiltroTodos.setOnAction(event -> loadData("TODOS"));
        btnFiltroActivos.setOnAction(event -> loadData("ACTIVOS"));
        btnFiltroDevueltos.setOnAction(event -> loadData("DEVUELTOS"));
        loadData("TODOS");
    }

    /**
     * Actualiza indicadores, productos y préstamos con información real.
     */
    private void loadData(String filter) {
        try {
            lblPrestamosActivos.setText(String.valueOf(loanService.countActive()));
            lblPrestamosDevueltos.setText(String.valueOf(loanService.countReturned()));

            paintProducts(loanService.findAvailableProducts());

            List<LoanView> loans = loanService.findAll();
            if ("ACTIVOS".equals(filter)) {
                loans = loans.stream()
                        .filter(loan -> "ACTIVO".equals(loan.getStatus())
                                || "VENCIDO".equals(loan.getStatus()))
                        .collect(Collectors.toList());
            } else if ("DEVUELTOS".equals(filter)) {
                loans = loans.stream()
                        .filter(loan -> "DEVUELTO".equals(loan.getStatus()))
                        .collect(Collectors.toList());
            }

            paintLoans(loans);
            paintSelectedFilter(filter);
        } catch (DatabaseException error) {
            showError("No se pudo actualizar Préstamos", error.getMessage());
        }
    }

    /**
     * Distribuye hasta seis productos en las dos filas de tarjetas.
     */
    private void paintProducts(List<CatalogItem> products) {
        filaProductos1.getChildren().clear();
        filaProductos2.getChildren().clear();

        for (int index = 0; index < products.size() && index < 6; index++) {
            HBox card = createProductCard(products.get(index));
            if (index < 3) {
                filaProductos1.getChildren().add(card);
            } else {
                filaProductos2.getChildren().add(card);
            }
        }
    }

    /**
     * Crea visualmente una tarjeta de producto a partir de un registro.
     */
    private HBox createProductCard(CatalogItem product) {
        StackPane image = new StackPane(new Label("◇"));
        image.setPrefSize(40, 40);
        image.setStyle("-fx-background-color: #ededed; -fx-border-color: #d0d0d0;");

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Label stock = new Label(product.getAvailable() + " disponibles");
        stock.setStyle("-fx-font-size: 9px; -fx-text-fill: #666666;");

        VBox text = new VBox(4, name, stock);
        HBox card = new HBox(12, image, text);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #d2d2d2; "
                + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 9;"
        );
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    /**
     * Sustituye las filas estáticas por las consultadas desde PostgreSQL.
     */
    private void paintLoans(List<LoanView> loans) {
        contenedorPrestamos.getChildren().clear();

        if (loans.isEmpty()) {
            Label empty = new Label("No existen préstamos para el filtro seleccionado.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setStyle("-fx-padding: 25; -fx-alignment: CENTER; -fx-text-fill: #666666;");
            contenedorPrestamos.getChildren().add(empty);
            return;
        }

        for (LoanView loan : loans) {
            contenedorPrestamos.getChildren().add(createLoanRow(loan));
        }
    }

    /**
     * Construye una fila con las mismas medidas del encabezado FXML.
     */
    private HBox createLoanRow(LoanView loan) {
        Label borrower = createCell(loan.getBorrower(), 185, true);
        Label product = createCell(loan.getProduct(), 230, false);
        Label quantity = createCell(String.valueOf(loan.getQuantity()), 90, true);
        Label loanDate = createCell(loan.getLoanDate().format(DATE_FORMAT), 170, true);
        Label dueDate = createCell(loan.getDueDate().format(DATE_FORMAT), 160, true);

        Label status = new Label(loan.getStatus().replace('_', ' '));
        status.setStyle(
                "-fx-background-color: #d3d3d3; -fx-background-radius: 10; "
                + "-fx-padding: 4 10; -fx-font-size: 8px;"
        );
        StackPane statusBox = new StackPane(status);
        statusBox.setPrefWidth(130);

        Button returnButton = new Button("Registrar\ndevolución");
        returnButton.setId("btnRegistrarDevolucion" + loan.getDetailId());
        returnButton.setStyle(
                "-fx-background-color: white; -fx-border-color: #cfcfcf; "
                + "-fx-border-radius: 4; -fx-background-radius: 4; "
                + "-fx-font-size: 9px; -fx-font-weight: bold;"
        );
        returnButton.setDisable(loan.getPendingQuantity() <= 0);
        returnButton.setOnAction(event -> registerReturn(loan));

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);
        HBox action = new HBox(8, actionSpacer, returnButton, new Region());
        action.setAlignment(Pos.CENTER);
        HBox.setHgrow(action, Priority.ALWAYS);

        HBox row = new HBox(
                borrower,
                product,
                quantity,
                loanDate,
                dueDate,
                statusBox,
                action
        );
        row.setPrefHeight(58);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent #dddddd transparent;");
        return row;
    }

    /** Crea una celda Label con ancho y alineación configurables. */
    private Label createCell(String text, double width, boolean centered) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle(
                "-fx-font-size: 9px; -fx-padding: 0 10; -fx-alignment: "
                + (centered ? "CENTER;" : "CENTER_LEFT;")
        );
        return label;
    }

    /**
     * Devuelve todas las unidades pendientes y vuelve a consultar la pantalla.
     */
    private void registerReturn(LoanView loan) {
        try {
            loanService.returnLoan(loan.getDetailId());
            showInformation(
                    "Devolución registrada",
                    loan.getProduct() + " regresó correctamente al inventario."
            );
            loadData("TODOS");
        } catch (DatabaseException error) {
            showError("No se pudo registrar la devolución", error.getMessage());
        }
    }

    /** Cambia únicamente el fondo del filtro seleccionado. */
    private void paintSelectedFilter(String filter) {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnFiltroTodos.setStyle("TODOS".equals(filter) ? selected : normal);
        btnFiltroActivos.setStyle("ACTIVOS".equals(filter) ? selected : normal);
        btnFiltroDevueltos.setStyle("DEVUELTOS".equals(filter) ? selected : normal);
    }

    /** Muestra un cuadro informativo después de una operación correcta. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra un error de persistencia sin cerrar la aplicación. */
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

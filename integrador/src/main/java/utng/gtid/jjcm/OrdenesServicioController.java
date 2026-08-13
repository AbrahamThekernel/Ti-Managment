package utng.gtid.jjcm;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;
import utng.gtid.jjcm.model.ServiceOrderView;
import utng.gtid.jjcm.repository.CatalogRepository;
import utng.gtid.jjcm.repository.ServiceOrderRepository;

/**
 * Controlador funcional de órdenes de servicio.
 */
public class OrdenesServicioController extends NavigationController {

    /** Formato corto utilizado por la columna FECHA. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT);

    /** Valores válidos para prioridad según schema.sql. */
    private static final List<String> PRIORITIES = Arrays.asList(
            "BAJA", "MEDIA", "ALTA", "URGENTE"
    );

    /** Acceso a SQL y a los catálogos de formularios. */
    private final ServiceOrderRepository orderRepository = new ServiceOrderRepository();
    private final CatalogRepository catalogRepository = new CatalogRepository();

    /** Órdenes recuperadas en la última consulta. */
    private List<ServiceOrderView> orders = new ArrayList<>();

    /** Filtro de estado activo. */
    private String currentFilter = "TODOS";

    /** Controles de búsqueda, filtros y creación. */
    @FXML private TextField txtBuscarOrden;
    @FXML private Button btnFiltroTodos;
    @FXML private Button btnFiltroAbiertas;
    @FXML private Button btnFiltroEnProceso;
    @FXML private Button btnFiltroCerradas;
    @FXML private Button btnNuevaOrden;

    /** Etiquetas de estadísticas calculadas con registros reales. */
    @FXML private Label lblOrdenesTotales;
    @FXML private Label lblOrdenesAbiertas;
    @FXML private Label lblOrdenesEnProceso;
    @FXML private Label lblOrdenesCerradas;
    @FXML private Label lblOrdenesCanceladas;

    /** Contenedor de filas dinámicas. */
    @FXML private VBox contenedorOrdenes;

    /** Configura eventos y carga inicial. */
    @FXML
    private void initialize() {
        btnFiltroTodos.setOnAction(event -> changeFilter("TODOS"));
        btnFiltroAbiertas.setOnAction(event -> changeFilter("ABIERTA"));
        btnFiltroEnProceso.setOnAction(event -> changeFilter("EN_PROCESO"));
        btnFiltroCerradas.setOnAction(event -> changeFilter("CERRADA"));
        btnNuevaOrden.setOnAction(event -> showNewOrderDialog());
        txtBuscarOrden.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );
        loadOrders();
    }

    /** Consulta PostgreSQL y refresca toda la pantalla. */
    private void loadOrders() {
        try {
            orders = orderRepository.findAll();
            updateCounters();
            applyFilters();
        } catch (DatabaseException error) {
            showError("No se pudieron cargar las órdenes", error.getMessage());
        }
    }

    /** Calcula los cinco indicadores por estado. */
    private void updateCounters() {
        lblOrdenesTotales.setText(String.valueOf(orders.size()));
        lblOrdenesAbiertas.setText(String.valueOf(countStatus("ABIERTA")));
        lblOrdenesEnProceso.setText(String.valueOf(countStatus("EN_PROCESO")));
        lblOrdenesCerradas.setText(String.valueOf(countStatus("CERRADA")));
        lblOrdenesCanceladas.setText(String.valueOf(countStatus("CANCELADA")));
    }

    /** Cuenta órdenes que coinciden con un estado. */
    private long countStatus(String status) {
        return orders.stream().filter(order -> status.equals(order.getStatus())).count();
    }

    /** Cambia el filtro seleccionado. */
    private void changeFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    /** Aplica estado y búsqueda libre sobre los datos cargados. */
    private void applyFilters() {
        String search = txtBuscarOrden.getText() == null
                ? ""
                : txtBuscarOrden.getText().trim().toLowerCase(Locale.ROOT);

        List<ServiceOrderView> filtered = orders.stream()
                .filter(order -> "TODOS".equals(currentFilter)
                        || currentFilter.equals(order.getStatus()))
                .filter(order -> search.isEmpty()
                        || order.getFolio().toLowerCase(Locale.ROOT).contains(search)
                        || order.getEquipment().toLowerCase(Locale.ROOT).contains(search)
                        || order.getRequester().toLowerCase(Locale.ROOT).contains(search)
                        || order.getServiceType().toLowerCase(Locale.ROOT).contains(search))
                .collect(Collectors.toList());

        paintRows(filtered);
        paintSelectedFilter();
    }

    /** Crea las filas de la tabla a partir del resultado filtrado. */
    private void paintRows(List<ServiceOrderView> filtered) {
        contenedorOrdenes.getChildren().clear();
        if (filtered.isEmpty()) {
            Label empty = new Label("No hay órdenes que coincidan con el filtro.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setStyle("-fx-padding: 28; -fx-alignment: CENTER; -fx-text-fill: #666666;");
            contenedorOrdenes.getChildren().add(empty);
            return;
        }
        filtered.forEach(order -> contenedorOrdenes.getChildren().add(createOrderRow(order)));
    }

    /** Construye una fila y sus acciones con ids únicos. */
    private HBox createOrderRow(ServiceOrderView order) {
        Label folio = createCell("▧  " + order.getFolio(), 140, true);
        Label date = createCell(order.getRequestDate().format(DATE_FORMAT), 110, true);
        Label equipment = createCell(order.getEquipment(), 190, false);
        Label requester = createCell(order.getRequester(), 150, false);
        Label type = createCell(order.getServiceType(), 130, false);

        Label status = new Label(formatEnum(order.getStatus()));
        status.setStyle("-fx-background-color: #d3d3d3; -fx-background-radius: 10; "
                + "-fx-padding: 4 9; -fx-font-size: 8px;");
        StackPane statusCell = new StackPane(status);
        statusCell.setPrefWidth(110);

        Label priority = createCell("●  " + formatEnum(order.getPriority()), 100, false);

        Button view = new Button("Ver");
        view.setId("btnVerOrden" + order.getId());
        view.setOnAction(event -> showOrderDetails(order));
        Button advance = new Button(nextStatusLabel(order.getStatus()));
        advance.setId("btnAvanzarOrden" + order.getId());
        advance.setDisable("CERRADA".equals(order.getStatus())
                || "CANCELADA".equals(order.getStatus()));
        advance.setOnAction(event -> advanceStatus(order));
        Button cancel = new Button("Cancelar");
        cancel.setId("btnCancelarOrden" + order.getId());
        cancel.setDisable("CERRADA".equals(order.getStatus())
                || "CANCELADA".equals(order.getStatus()));
        cancel.setOnAction(event -> updateStatus(order, "CANCELADA"));

        HBox actions = new HBox(5, view, advance, cancel);
        actions.setAlignment(Pos.CENTER);
        HBox.setHgrow(actions, Priority.ALWAYS);

        HBox row = new HBox(folio, date, equipment, requester, type,
                statusCell, priority, actions);
        row.setPrefHeight(56);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent #dedede transparent;");
        return row;
    }

    /** Crea una celda de texto con las mismas medidas del encabezado. */
    private Label createCell(String text, double width, boolean centered) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 9px; -fx-padding: 0 7; -fx-alignment: "
                + (centered ? "CENTER;" : "CENTER_LEFT;"));
        return label;
    }

    /** Abre un formulario y registra una orden en PostgreSQL. */
    private void showNewOrderDialog() {
        try {
            List<CatalogItem> equipment = catalogRepository.findUsableEquipment();
            List<CatalogItem> requesters = catalogRepository.findActiveBorrowers();
            List<CatalogItem> technicians = catalogRepository.findActiveTechnicians();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nueva orden de servicio");
            dialog.setHeaderText("Describe el servicio solicitado");
            ButtonType saveType = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            ComboBox<CatalogItem> equipmentBox = catalogBox("cmbEquipoOrden", equipment);
            ComboBox<CatalogItem> requesterBox = catalogBox("cmbSolicitanteOrden", requesters);
            ComboBox<CatalogItem> responsibleBox = catalogBox("cmbResponsableOrden", technicians);
            ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList(PRIORITIES));
            priorityBox.setId("cmbPrioridadOrden");
            priorityBox.setValue("MEDIA");
            priorityBox.setMaxWidth(Double.MAX_VALUE);
            TextField serviceType = new TextField();
            serviceType.setId("txtTipoServicioOrden");
            serviceType.setPromptText("Reparación, instalación, ajuste...");
            TextArea description = new TextArea();
            description.setId("txtDescripcionOrden");
            description.setPromptText("Describe el problema o trabajo requerido");
            description.setPrefRowCount(3);
            description.setWrapText(true);

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(9);
            form.setPadding(new Insets(10));
            form.addRow(0, new Label("Equipo:"), equipmentBox);
            form.addRow(1, new Label("Solicitante:"), requesterBox);
            form.addRow(2, new Label("Responsable:"), responsibleBox);
            form.addRow(3, new Label("Tipo de servicio:"), serviceType);
            form.addRow(4, new Label("Prioridad:"), priorityBox);
            form.addRow(5, new Label("Descripción:"), description);
            dialog.getDialogPane().setContent(form);

            boolean saved = false;
            while (!saved) {
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isEmpty() || result.get() != saveType) return;
                try {
                    validateAndInsert(equipmentBox.getValue(), requesterBox.getValue(),
                            responsibleBox.getValue(), serviceType.getText(),
                            description.getText(), priorityBox.getValue());
                    saved = true;
                } catch (IllegalArgumentException error) {
                    showError("Datos incorrectos", error.getMessage());
                }
            }
            loadOrders();
        } catch (DatabaseException error) {
            showError("No se pudo registrar la orden", error.getMessage());
        }
    }

    /** Crea y preselecciona un ComboBox de catálogo con id explícito. */
    private ComboBox<CatalogItem> catalogBox(String id, List<CatalogItem> values) {
        ComboBox<CatalogItem> box = new ComboBox<>(FXCollections.observableArrayList(values));
        box.setId(id);
        box.setMaxWidth(Double.MAX_VALUE);
        if (!values.isEmpty()) box.getSelectionModel().selectFirst();
        return box;
    }

    /** Valida el formulario y crea una orden con folio único legible. */
    private void validateAndInsert(
            CatalogItem equipment,
            CatalogItem requester,
            CatalogItem responsible,
            String serviceType,
            String description,
            String priority
    ) {
        if (equipment == null || requester == null) {
            throw new IllegalArgumentException("Selecciona equipo y solicitante.");
        }
        if (serviceType == null || serviceType.isBlank()
                || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Tipo de servicio y descripción son obligatorios.");
        }

        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String folio = "OS-" + Year.now().getValue() + "-" + suffix;
        orderRepository.insert(folio, equipment.getId(), requester.getId(),
                responsible == null ? null : responsible.getId(),
                serviceType.trim(), description.trim(), priority);
    }

    /** Muestra la información completa de una orden. */
    private void showOrderDetails(ServiceOrderView order) {
        showInformation(
                "Orden " + order.getFolio(),
                "Equipo: " + order.getEquipment()
                + "\nSolicitante: " + order.getRequester()
                + "\nResponsable: " + order.getResponsible()
                + "\nServicio: " + order.getServiceType()
                + "\nPrioridad: " + formatEnum(order.getPriority())
                + "\nEstado: " + formatEnum(order.getStatus())
                + "\nDescripción: " + order.getDescription()
        );
    }

    /** Avanza ABIERTA a EN_PROCESO y EN_PROCESO a CERRADA. */
    private void advanceStatus(ServiceOrderView order) {
        String next = "ABIERTA".equals(order.getStatus()) ? "EN_PROCESO" : "CERRADA";
        updateStatus(order, next);
    }

    /** Persiste un estado y vuelve a consultar los datos. */
    private void updateStatus(ServiceOrderView order, String status) {
        try {
            orderRepository.updateStatus(order.getId(), status);
            loadOrders();
        } catch (DatabaseException error) {
            showError("No se pudo actualizar la orden", error.getMessage());
        }
    }

    /** Devuelve el texto de la siguiente acción posible. */
    private String nextStatusLabel(String status) {
        if ("ABIERTA".equals(status)) return "Iniciar";
        if ("EN_PROCESO".equals(status)) return "Cerrar";
        return "Finalizada";
    }

    /** Convierte enums de PostgreSQL en texto legible. */
    private String formatEnum(String value) {
        String text = value.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Marca visualmente el filtro activo. */
    private void paintSelectedFilter() {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnFiltroTodos.setStyle("TODOS".equals(currentFilter) ? selected : normal);
        btnFiltroAbiertas.setStyle("ABIERTA".equals(currentFilter) ? selected : normal);
        btnFiltroEnProceso.setStyle("EN_PROCESO".equals(currentFilter) ? selected : normal);
        btnFiltroCerradas.setStyle("CERRADA".equals(currentFilter) ? selected : normal);
    }

    /** Muestra información de lectura. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra errores de formulario o persistencia. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

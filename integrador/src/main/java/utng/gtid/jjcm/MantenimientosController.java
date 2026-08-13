package utng.gtid.jjcm;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
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
import utng.gtid.jjcm.model.MaintenanceView;
import utng.gtid.jjcm.repository.CatalogRepository;
import utng.gtid.jjcm.repository.MaintenanceRepository;

/**
 * Controlador de programación y seguimiento de mantenimientos.
 */
public class MantenimientosController extends NavigationController {

    /** Formato de fecha utilizado en la tabla. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT);

    /** Tipos y prioridades aceptados por las restricciones de PostgreSQL. */
    private static final List<String> TYPES = Arrays.asList("PREVENTIVO", "CORRECTIVO");
    private static final List<String> PRIORITIES = Arrays.asList("BAJA", "MEDIA", "ALTA");

    /** Repositorios de mantenimientos y listas desplegables. */
    private final MaintenanceRepository maintenanceRepository = new MaintenanceRepository();
    private final CatalogRepository catalogRepository = new CatalogRepository();

    /** Último listado recuperado desde PostgreSQL. */
    private List<MaintenanceView> maintenance = new ArrayList<>();

    /** Estado seleccionado en los botones de filtro. */
    private String currentFilter = "TODOS";

    /** Controles principales enlazados desde mantenimientos.fxml. */
    @FXML private TextField txtBuscarMantenimiento;
    @FXML private Button btnFiltroTodos;
    @FXML private Button btnFiltroProgramados;
    @FXML private Button btnFiltroEnProceso;
    @FXML private Button btnFiltroCompletados;
    @FXML private Button btnNuevoMantenimiento;

    /** Indicadores calculados a partir del resultado real. */
    @FXML private Label lblMantenimientosTotales;
    @FXML private Label lblMantenimientosProgramados;
    @FXML private Label lblMantenimientosEnProceso;
    @FXML private Label lblMantenimientosCompletados;
    @FXML private Label lblMantenimientosCancelados;

    /** Contenedor de filas creadas por el controlador. */
    @FXML private VBox contenedorMantenimientos;

    /** Configura eventos y carga inicial. */
    @FXML
    private void initialize() {
        btnFiltroTodos.setOnAction(event -> changeFilter("TODOS"));
        btnFiltroProgramados.setOnAction(event -> changeFilter("PROGRAMADO"));
        btnFiltroEnProceso.setOnAction(event -> changeFilter("EN_PROCESO"));
        btnFiltroCompletados.setOnAction(event -> changeFilter("COMPLETADO"));
        btnNuevoMantenimiento.setOnAction(event -> showNewMaintenanceDialog());
        txtBuscarMantenimiento.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );
        loadMaintenance();
    }

    /** Consulta PostgreSQL y actualiza indicadores y tabla. */
    private void loadMaintenance() {
        try {
            maintenance = maintenanceRepository.findAll();
            updateCounters();
            applyFilters();
        } catch (DatabaseException error) {
            showError("No se pudieron cargar los mantenimientos", error.getMessage());
        }
    }

    /** Cuenta cada estado para las tarjetas superiores. */
    private void updateCounters() {
        lblMantenimientosTotales.setText(String.valueOf(maintenance.size()));
        lblMantenimientosProgramados.setText(String.valueOf(countStatus("PROGRAMADO")));
        lblMantenimientosEnProceso.setText(String.valueOf(countStatus("EN_PROCESO")));
        lblMantenimientosCompletados.setText(String.valueOf(countStatus("COMPLETADO")));
        lblMantenimientosCancelados.setText(String.valueOf(countStatus("CANCELADO")));
    }

    /** Cuenta registros con un estado determinado. */
    private long countStatus(String status) {
        return maintenance.stream().filter(item -> status.equals(item.getStatus())).count();
    }

    /** Cambia el filtro de estado. */
    private void changeFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    /** Filtra por estado y texto sin repetir la consulta SQL. */
    private void applyFilters() {
        String search = txtBuscarMantenimiento.getText() == null
                ? ""
                : txtBuscarMantenimiento.getText().trim().toLowerCase(Locale.ROOT);

        List<MaintenanceView> filtered = maintenance.stream()
                .filter(item -> "TODOS".equals(currentFilter)
                        || currentFilter.equals(item.getStatus()))
                .filter(item -> search.isEmpty()
                        || item.getFolio().toLowerCase(Locale.ROOT).contains(search)
                        || item.getEquipment().toLowerCase(Locale.ROOT).contains(search)
                        || item.getTechnician().toLowerCase(Locale.ROOT).contains(search)
                        || item.getType().toLowerCase(Locale.ROOT).contains(search))
                .collect(Collectors.toList());

        paintRows(filtered);
        paintSelectedFilter();
    }

    /** Sustituye las filas de ejemplo por filas de base de datos. */
    private void paintRows(List<MaintenanceView> filtered) {
        contenedorMantenimientos.getChildren().clear();
        if (filtered.isEmpty()) {
            Label empty = new Label("No hay mantenimientos que coincidan con el filtro.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setStyle("-fx-padding: 28; -fx-alignment: CENTER; -fx-text-fill: #666666;");
            contenedorMantenimientos.getChildren().add(empty);
            return;
        }
        filtered.forEach(item ->
                contenedorMantenimientos.getChildren().add(createMaintenanceRow(item))
        );
    }

    /** Construye una fila y botones normales con ids únicos. */
    private HBox createMaintenanceRow(MaintenanceView item) {
        Label folio = createCell("▧  " + item.getFolio(), 145, true);
        Label date = createCell(item.getScheduledDate().format(DATE_FORMAT), 145, true);
        Label equipment = createCell(item.getEquipment(), 210, false);
        Label type = createCell(formatEnum(item.getType()), 160, false);
        Label technician = createCell(item.getTechnician(), 155, false);

        Label status = new Label(formatEnum(item.getStatus()));
        status.setStyle("-fx-background-color: #d3d3d3; -fx-background-radius: 10; "
                + "-fx-padding: 4 8; -fx-font-size: 8px;");
        StackPane statusCell = new StackPane(status);
        statusCell.setPrefWidth(110);

        Label priority = createCell("●  " + formatEnum(item.getPriority()), 90, false);

        Button view = new Button("Ver");
        view.setId("btnVerMantenimiento" + item.getId());
        view.setOnAction(event -> showMaintenanceDetails(item));
        Button advance = new Button(nextStatusLabel(item.getStatus()));
        advance.setId("btnAvanzarMantenimiento" + item.getId());
        advance.setDisable(isFinal(item.getStatus()));
        advance.setOnAction(event -> advanceStatus(item));
        Button cancel = new Button("Cancelar");
        cancel.setId("btnCancelarMantenimiento" + item.getId());
        cancel.setDisable(isFinal(item.getStatus()));
        cancel.setOnAction(event -> updateStatus(item, "CANCELADO"));

        HBox actions = new HBox(5, view, advance, cancel);
        actions.setAlignment(Pos.CENTER);
        HBox.setHgrow(actions, Priority.ALWAYS);

        HBox row = new HBox(folio, date, equipment, type, technician,
                statusCell, priority, actions);
        row.setPrefHeight(56);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent #dedede transparent;");
        return row;
    }

    /** Crea una celda Label ajustada al encabezado. */
    private Label createCell(String text, double width, boolean centered) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 9px; -fx-padding: 0 7; -fx-alignment: "
                + (centered ? "CENTER;" : "CENTER_LEFT;"));
        return label;
    }

    /** Abre el formulario y registra un mantenimiento PROGRAMADO. */
    private void showNewMaintenanceDialog() {
        try {
            List<CatalogItem> equipment = catalogRepository.findUsableEquipment();
            List<CatalogItem> technicians = catalogRepository.findActiveTechnicians();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nuevo mantenimiento");
            dialog.setHeaderText("Programa el trabajo preventivo o correctivo");
            ButtonType saveType = new ButtonType("Programar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            ComboBox<CatalogItem> equipmentBox = catalogBox("cmbEquipoMantenimiento", equipment);
            ComboBox<CatalogItem> technicianBox = catalogBox("cmbTecnicoMantenimiento", technicians);
            ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(TYPES));
            typeBox.setId("cmbTipoMantenimiento");
            typeBox.setValue("PREVENTIVO");
            ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList(PRIORITIES));
            priorityBox.setId("cmbPrioridadMantenimiento");
            priorityBox.setValue("MEDIA");
            typeBox.setMaxWidth(Double.MAX_VALUE);
            priorityBox.setMaxWidth(Double.MAX_VALUE);
            DatePicker date = new DatePicker(LocalDate.now().plusDays(1));
            date.setId("fechaProgramadaMantenimiento");
            TextArea diagnosis = new TextArea();
            diagnosis.setId("txtDiagnosticoMantenimiento");
            diagnosis.setPrefRowCount(3);
            diagnosis.setWrapText(true);
            TextField cost = new TextField("0.00");
            cost.setId("txtCostoMantenimiento");

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(9);
            form.setPadding(new Insets(10));
            form.addRow(0, new Label("Equipo:"), equipmentBox);
            form.addRow(1, new Label("Técnico:"), technicianBox);
            form.addRow(2, new Label("Tipo:"), typeBox);
            form.addRow(3, new Label("Fecha programada:"), date);
            form.addRow(4, new Label("Prioridad:"), priorityBox);
            form.addRow(5, new Label("Diagnóstico inicial:"), diagnosis);
            form.addRow(6, new Label("Costo estimado:"), cost);
            dialog.getDialogPane().setContent(form);

            boolean saved = false;
            while (!saved) {
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isEmpty() || result.get() != saveType) return;
                try {
                    validateAndInsert(equipmentBox.getValue(), technicianBox.getValue(),
                            typeBox.getValue(), date.getValue(), priorityBox.getValue(),
                            diagnosis.getText(), cost.getText());
                    saved = true;
                } catch (IllegalArgumentException error) {
                    showError("Datos incorrectos", error.getMessage());
                }
            }
            loadMaintenance();
        } catch (DatabaseException error) {
            showError("No se pudo programar el mantenimiento", error.getMessage());
        }
    }

    /** Crea un ComboBox de catálogo, le asigna id y preselecciona el primero. */
    private ComboBox<CatalogItem> catalogBox(String id, List<CatalogItem> values) {
        ComboBox<CatalogItem> box = new ComboBox<>(FXCollections.observableArrayList(values));
        box.setId(id);
        box.setMaxWidth(Double.MAX_VALUE);
        if (!values.isEmpty()) box.getSelectionModel().selectFirst();
        return box;
    }

    /** Valida el formulario y ejecuta el INSERT. */
    private void validateAndInsert(
            CatalogItem equipment,
            CatalogItem technician,
            String type,
            LocalDate scheduledDate,
            String priority,
            String diagnosis,
            String costText
    ) {
        if (equipment == null) {
            throw new IllegalArgumentException("Selecciona un equipo.");
        }
        if (type == null || scheduledDate == null || priority == null) {
            throw new IllegalArgumentException("Tipo, fecha y prioridad son obligatorios.");
        }
        if (scheduledDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha programada no puede estar en el pasado.");
        }

        BigDecimal cost;
        try {
            cost = new BigDecimal(costText.trim());
            if (cost.signum() < 0) throw new NumberFormatException("negative");
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("El costo debe ser un número igual o mayor que cero.");
        }

        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String folio = "MTTO-" + Year.now().getValue() + "-" + suffix;
        maintenanceRepository.insert(folio, equipment.getId(),
                technician == null ? null : technician.getId(), type,
                scheduledDate, priority, diagnosis, cost);
    }

    /** Muestra todos los detalles del mantenimiento. */
    private void showMaintenanceDetails(MaintenanceView item) {
        showInformation(
                "Mantenimiento " + item.getFolio(),
                "Equipo: " + item.getEquipment()
                + "\nTipo: " + formatEnum(item.getType())
                + "\nTécnico: " + item.getTechnician()
                + "\nFecha: " + item.getScheduledDate().format(DATE_FORMAT)
                + "\nPrioridad: " + formatEnum(item.getPriority())
                + "\nEstado: " + formatEnum(item.getStatus())
                + "\nDiagnóstico: " + item.getDiagnosis()
                + "\nTrabajo realizado: " + item.getCompletedWork()
                + "\nCosto: $" + item.getCost()
        );
    }

    /** Avanza PROGRAMADO a EN_PROCESO y luego a COMPLETADO. */
    private void advanceStatus(MaintenanceView item) {
        String next = "PROGRAMADO".equals(item.getStatus()) ? "EN_PROCESO" : "COMPLETADO";
        updateStatus(item, next);
    }

    /** Actualiza mantenimiento y equipo dentro de la transacción del repositorio. */
    private void updateStatus(MaintenanceView item, String status) {
        try {
            maintenanceRepository.updateStatus(item.getId(), item.getEquipmentId(), status);
            loadMaintenance();
        } catch (DatabaseException error) {
            showError("No se pudo actualizar el mantenimiento", error.getMessage());
        }
    }

    /** Indica si ya no se permiten transiciones. */
    private boolean isFinal(String status) {
        return "COMPLETADO".equals(status) || "CANCELADO".equals(status);
    }

    /** Devuelve el texto de la siguiente acción. */
    private String nextStatusLabel(String status) {
        if ("PROGRAMADO".equals(status)) return "Iniciar";
        if ("EN_PROCESO".equals(status)) return "Completar";
        return "Finalizado";
    }

    /** Convierte enums con guiones bajos a texto legible. */
    private String formatEnum(String value) {
        String text = value.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Marca el filtro seleccionado. */
    private void paintSelectedFilter() {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnFiltroTodos.setStyle("TODOS".equals(currentFilter) ? selected : normal);
        btnFiltroProgramados.setStyle("PROGRAMADO".equals(currentFilter) ? selected : normal);
        btnFiltroEnProceso.setStyle("EN_PROCESO".equals(currentFilter) ? selected : normal);
        btnFiltroCompletados.setStyle("COMPLETADO".equals(currentFilter) ? selected : normal);
    }

    /** Muestra información de lectura. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra errores de validación o persistencia. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package utng.gtid.jjcm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import utng.gtid.jjcm.model.EquipmentView;
import utng.gtid.jjcm.repository.CatalogRepository;
import utng.gtid.jjcm.repository.EquipmentRepository;

/**
 * Controlador CRUD para activos individuales de la pantalla Equipos.
 */
public class EquiposController extends NavigationController {

    /** Estados permitidos por la restricción CHECK de PostgreSQL. */
    private static final List<String> VALID_STATES = Arrays.asList(
            "ACTIVO", "MANTENIMIENTO", "BAJA", "SIN_ASIGNAR"
    );

    /** Repositorios de equipos y catálogos relacionados. */
    private final EquipmentRepository equipmentRepository = new EquipmentRepository();
    private final CatalogRepository catalogRepository = new CatalogRepository();

    /** Equipos obtenidos en la última consulta. */
    private List<EquipmentView> equipment = new ArrayList<>();

    /** Estado de filtro actualmente seleccionado. */
    private String currentFilter = "TODOS";

    /** Controles de búsqueda y acciones definidos en equipos.fxml. */
    @FXML private TextField txtBuscarEquipo;
    @FXML private Button btnFiltroTodos;
    @FXML private Button btnFiltroActivos;
    @FXML private Button btnFiltroEnMantenimiento;
    @FXML private Button btnFiltroDadosBaja;
    @FXML private Button btnNuevoEquipo;

    /** Indicadores superiores calculados desde la lista real. */
    @FXML private Label lblTotalEquipos;
    @FXML private Label lblEquiposActivos;
    @FXML private Label lblEquiposMantenimiento;
    @FXML private Label lblEquiposBaja;
    @FXML private Label lblEquiposSinAsignar;

    /** Contenedor que recibe las filas creadas por el controlador. */
    @FXML private VBox contenedorEquipos;

    /** Configura filtros, búsqueda, alta y carga inicial. */
    @FXML
    private void initialize() {
        btnFiltroTodos.setOnAction(event -> changeFilter("TODOS"));
        btnFiltroActivos.setOnAction(event -> changeFilter("ACTIVO"));
        btnFiltroEnMantenimiento.setOnAction(event -> changeFilter("MANTENIMIENTO"));
        btnFiltroDadosBaja.setOnAction(event -> changeFilter("BAJA"));
        btnNuevoEquipo.setOnAction(event -> showEquipmentDialog(null));
        txtBuscarEquipo.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );
        loadEquipment();
    }

    /** Consulta PostgreSQL, actualiza indicadores y dibuja la tabla. */
    private void loadEquipment() {
        try {
            equipment = equipmentRepository.findAll();
            updateCounters();
            applyFilters();
        } catch (DatabaseException error) {
            showError("No se pudieron cargar los equipos", error.getMessage());
        }
    }

    /** Cuenta todos los estados mostrados en las tarjetas superiores. */
    private void updateCounters() {
        lblTotalEquipos.setText(String.valueOf(equipment.size()));
        lblEquiposActivos.setText(String.valueOf(countState("ACTIVO")));
        lblEquiposMantenimiento.setText(String.valueOf(countState("MANTENIMIENTO")));
        lblEquiposBaja.setText(String.valueOf(countState("BAJA")));
        lblEquiposSinAsignar.setText(String.valueOf(
                equipment.stream().filter(item -> item.getLocationId() == null
                        || "SIN_ASIGNAR".equals(item.getStatus())).count()
        ));
    }

    /** Cuenta cuántos equipos tienen el estado recibido. */
    private long countState(String state) {
        return equipment.stream().filter(item -> state.equals(item.getStatus())).count();
    }

    /** Cambia el filtro y vuelve a pintar únicamente la vista local. */
    private void changeFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    /** Aplica simultáneamente estado y búsqueda libre. */
    private void applyFilters() {
        String search = txtBuscarEquipo.getText() == null
                ? ""
                : txtBuscarEquipo.getText().trim().toLowerCase(Locale.ROOT);

        List<EquipmentView> filtered = equipment.stream()
                .filter(item -> "TODOS".equals(currentFilter)
                        || currentFilter.equals(item.getStatus()))
                .filter(item -> search.isEmpty()
                        || item.getCode().toLowerCase(Locale.ROOT).contains(search)
                        || item.getName().toLowerCase(Locale.ROOT).contains(search)
                        || item.getModel().toLowerCase(Locale.ROOT).contains(search)
                        || item.getSerialNumber().toLowerCase(Locale.ROOT).contains(search))
                .collect(Collectors.toList());

        paintRows(filtered);
        paintSelectedFilter();
    }

    /** Sustituye las filas de ejemplo por registros de PostgreSQL. */
    private void paintRows(List<EquipmentView> filtered) {
        contenedorEquipos.getChildren().clear();
        if (filtered.isEmpty()) {
            Label empty = new Label("No hay equipos que coincidan con el filtro.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setStyle("-fx-padding: 28; -fx-alignment: CENTER; -fx-text-fill: #666666;");
            contenedorEquipos.getChildren().add(empty);
            return;
        }
        filtered.forEach(item -> contenedorEquipos.getChildren().add(createEquipmentRow(item)));
    }

    /** Construye una fila y asigna id a cada botón normal. */
    private HBox createEquipmentRow(EquipmentView item) {
        Label code = createCell("▧  " + item.getCode(), 135, true);
        Label name = createCell(item.getName(), 205, false);
        Label category = createCell(item.getCategory(), 145, false);
        Label model = createCell(item.getModel(), 145, false);
        Label serial = createCell(item.getSerialNumber(), 150, false);

        Label state = new Label(formatEnum(item.getStatus()));
        state.setStyle("-fx-background-color: #d3d3d3; -fx-background-radius: 10; "
                + "-fx-padding: 4 9; -fx-font-size: 8px;");
        StackPane stateCell = new StackPane(state);
        stateCell.setPrefWidth(115);

        Label location = createCell("●  " + item.getLocation(), 120, false);

        Button viewButton = new Button("Ver");
        viewButton.setId("btnVerEquipo" + item.getId());
        viewButton.setOnAction(event -> showEquipmentDetails(item));
        Button editButton = new Button("Editar");
        editButton.setId("btnEditarEquipo" + item.getId());
        editButton.setOnAction(event -> showEquipmentDialog(item));

        HBox actions = new HBox(7, viewButton, editButton);
        actions.setAlignment(Pos.CENTER);
        HBox.setHgrow(actions, Priority.ALWAYS);

        HBox row = new HBox(code, name, category, model, serial, stateCell, location, actions);
        row.setPrefHeight(56);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent #dedede transparent;");
        return row;
    }

    /** Crea una celda Label con el ancho de su encabezado. */
    private Label createCell(String text, double width, boolean centered) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 9px; -fx-padding: 0 8; -fx-alignment: "
                + (centered ? "CENTER;" : "CENTER_LEFT;"));
        return label;
    }

    /** Muestra todos los detalles sin modificar la base. */
    private void showEquipmentDetails(EquipmentView item) {
        showInformation(
                "Detalle de " + item.getCode(),
                "Equipo: " + item.getName()
                + "\nCategoría: " + item.getCategory()
                + "\nModelo: " + item.getModel()
                + "\nSerie: " + item.getSerialNumber()
                + "\nEstado: " + formatEnum(item.getStatus())
                + "\nUbicación: " + item.getLocation()
                + "\nObservaciones: " + item.getObservations()
        );
    }

    /** Abre el formulario reutilizable de alta o edición. */
    private void showEquipmentDialog(EquipmentView item) {
        try {
            List<CatalogItem> categories = catalogRepository.findActiveCategories();
            List<CatalogItem> locations = catalogRepository.findActiveLocations();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(item == null ? "Nuevo equipo" : "Editar equipo");
            dialog.setHeaderText("Datos del activo institucional");
            ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            ComboBox<CatalogItem> category = new ComboBox<>(FXCollections.observableArrayList(categories));
            category.setId("cmbCategoriaEquipo");
            ComboBox<CatalogItem> location = new ComboBox<>(FXCollections.observableArrayList(locations));
            location.setId("cmbUbicacionEquipo");
            ComboBox<String> state = new ComboBox<>(FXCollections.observableArrayList(VALID_STATES));
            state.setId("cmbEstadoEquipo");
            category.setMaxWidth(Double.MAX_VALUE);
            location.setMaxWidth(Double.MAX_VALUE);
            state.setMaxWidth(Double.MAX_VALUE);

            TextField code = createField("txtCodigoEquipo", "EQ-2026-000");
            TextField name = createField("txtNombreEquipo", "Nombre del equipo");
            TextField model = createField("txtModeloEquipo", "Modelo opcional");
            TextField serial = createField("txtSerieEquipo", "Número de serie opcional");
            TextArea observations = new TextArea();
            observations.setId("txtObservacionesEquipo");
            observations.setPrefRowCount(2);
            observations.setWrapText(true);

            fillExistingEquipment(item, categories, locations, category, location,
                    state, code, name, model, serial, observations);

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(9);
            form.setPadding(new Insets(10));
            form.addRow(0, new Label("Código:"), code);
            form.addRow(1, new Label("Nombre:"), name);
            form.addRow(2, new Label("Categoría:"), category);
            form.addRow(3, new Label("Modelo:"), model);
            form.addRow(4, new Label("Número de serie:"), serial);
            form.addRow(5, new Label("Estado:"), state);
            form.addRow(6, new Label("Ubicación:"), location);
            form.addRow(7, new Label("Observaciones:"), observations);
            dialog.getDialogPane().setContent(form);

            boolean saved = false;
            while (!saved) {
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isEmpty() || result.get() != saveType) {
                    return;
                }
                try {
                    saveEquipment(item, category.getValue(), location.getValue(), state.getValue(),
                            code.getText(), name.getText(), model.getText(), serial.getText(),
                            observations.getText());
                    saved = true;
                } catch (IllegalArgumentException error) {
                    showError("Datos incorrectos", error.getMessage());
                }
            }
            loadEquipment();
        } catch (DatabaseException error) {
            showError("No se pudo guardar el equipo", error.getMessage());
        }
    }

    /** Crea un TextField normal y le asigna un id. */
    private TextField createField(String id, String prompt) {
        TextField field = new TextField();
        field.setId(id);
        field.setPromptText(prompt);
        field.setPrefWidth(290);
        return field;
    }

    /** Coloca datos previos o valores iniciales en el formulario. */
    private void fillExistingEquipment(
            EquipmentView item,
            List<CatalogItem> categories,
            List<CatalogItem> locations,
            ComboBox<CatalogItem> category,
            ComboBox<CatalogItem> location,
            ComboBox<String> state,
            TextField code,
            TextField name,
            TextField model,
            TextField serial,
            TextArea observations
    ) {
        if (item == null) {
            if (!categories.isEmpty()) category.getSelectionModel().selectFirst();
            if (!locations.isEmpty()) location.getSelectionModel().selectFirst();
            state.setValue("ACTIVO");
            return;
        }
        categories.stream().filter(value -> value.getId() == item.getCategoryId()).findFirst()
                .ifPresent(category.getSelectionModel()::select);
        if (item.getLocationId() != null) {
            locations.stream().filter(value -> value.getId() == item.getLocationId()).findFirst()
                    .ifPresent(location.getSelectionModel()::select);
        }
        state.setValue(item.getStatus());
        code.setText(item.getCode());
        name.setText(item.getName());
        model.setText(item.getModel());
        serial.setText(item.getSerialNumber());
        observations.setText(item.getObservations());
    }

    /** Valida y ejecuta INSERT o UPDATE. */
    private void saveEquipment(
            EquipmentView item,
            CatalogItem category,
            CatalogItem location,
            String state,
            String code,
            String name,
            String model,
            String serial,
            String observations
    ) {
        if (category == null) {
            throw new IllegalArgumentException("Selecciona una categoría.");
        }
        if (state == null) {
            throw new IllegalArgumentException("Selecciona un estado.");
        }
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Código y nombre son obligatorios.");
        }
        Long locationId = location == null ? null : location.getId();
        if (item == null) {
            equipmentRepository.insert(category.getId(), locationId, code.trim(), name.trim(),
                    model, serial, state, observations);
        } else {
            equipmentRepository.update(item.getId(), category.getId(), locationId,
                    code.trim(), name.trim(), model, serial, state, observations);
        }
    }

    /** Convierte el enum de base de datos a texto legible. */
    private String formatEnum(String value) {
        String text = value.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Cambia únicamente la apariencia del filtro seleccionado. */
    private void paintSelectedFilter() {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnFiltroTodos.setStyle("TODOS".equals(currentFilter) ? selected : normal);
        btnFiltroActivos.setStyle("ACTIVO".equals(currentFilter) ? selected : normal);
        btnFiltroEnMantenimiento.setStyle(
                "MANTENIMIENTO".equals(currentFilter) ? selected : normal
        );
        btnFiltroDadosBaja.setStyle("BAJA".equals(currentFilter) ? selected : normal);
    }

    /** Muestra información de un equipo. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra errores de validación o base de datos. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package utng.gtid.jjcm;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.BackupView;
import utng.gtid.jjcm.service.BackupService;

/**
 * Controlador de respaldos reales mediante pg_dump y pg_restore.
 */
public class RespaldosController extends NavigationController {

    /** Formato de fecha y hora visible en el historial. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ROOT);

    /** Servicio que ejecuta las herramientas oficiales de PostgreSQL. */
    private final BackupService backupService = new BackupService();

    /** Historial obtenido desde la tabla respaldos. */
    private List<BackupView> backups = new ArrayList<>();

    /** Filtro TODOS, AUTOMATICO o MANUAL. */
    private String currentFilter = "TODOS";

    /** Controles de búsqueda, filtros y alta. */
    @FXML private TextField txtBuscarRespaldo;
    @FXML private Button btnFiltroTodos;
    @FXML private Button btnFiltroAutomaticos;
    @FXML private Button btnFiltroManuales;
    @FXML private Button btnConfiguracionRespaldos;
    @FXML private Button btnNuevoRespaldo;
    @FXML private Button btnAlternarAutomaticos;
    @FXML private Button btnCambiarDestino;
    @FXML private Button btnCambiarRetencion;

    /** Indicadores calculados desde los metadatos reales. */
    @FXML private Label lblRespaldosTotales;
    @FXML private Label lblRespaldosExitosos;
    @FXML private Label lblRespaldosFallidos;
    @FXML private Label lblAlmacenamientoRespaldos;

    /** Contenedor de filas dinámicas. */
    @FXML private VBox contenedorRespaldos;

    /** Configura eventos y carga el historial. */
    @FXML
    private void initialize() {
        btnFiltroTodos.setOnAction(event -> changeFilter("TODOS"));
        btnFiltroAutomaticos.setOnAction(event -> changeFilter("AUTOMATICO"));
        btnFiltroManuales.setOnAction(event -> changeFilter("MANUAL"));
        btnNuevoRespaldo.setOnAction(event -> chooseAndCreateBackup());
        txtBuscarRespaldo.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        // La automatización permanente debe configurarse en pgAgent o el sistema operativo.
        btnAlternarAutomaticos.setText("Configurar");
        btnAlternarAutomaticos.setOnAction(event -> showInformation(
                "Respaldos automáticos",
                "El respaldo manual ya funciona. Para ejecuciones sin abrir la aplicación usa pgAgent en pgAdmin 4."
        ));
        btnConfiguracionRespaldos.setOnAction(event -> btnAlternarAutomaticos.fire());
        btnCambiarDestino.setOnAction(event -> showInformation(
                "Destino de respaldo",
                "La ruta se elige al crear o descargar cada respaldo para evitar escribir en una carpeta equivocada."
        ));
        btnCambiarRetencion.setOnAction(event -> showInformation(
                "Retención",
                "Los archivos no se eliminan automáticamente; usa Eliminar y confirma cada archivo exacto."
        ));

        loadBackups();
    }

    /** Consulta PostgreSQL y actualiza estadísticas y filas. */
    private void loadBackups() {
        try {
            backups = backupService.findAll();
            updateCounters();
            applyFilters();
        } catch (DatabaseException error) {
            showError("No se pudieron cargar los respaldos", error.getMessage());
        }
    }

    /** Calcula total, éxito, fallos y tamaño acumulado. */
    private void updateCounters() {
        lblRespaldosTotales.setText(String.valueOf(backups.size()));
        lblRespaldosExitosos.setText(String.valueOf(
                backups.stream().filter(item -> "COMPLETADO".equals(item.getStatus())).count()
        ));
        lblRespaldosFallidos.setText(String.valueOf(
                backups.stream().filter(item -> "FALLIDO".equals(item.getStatus())).count()
        ));
        long totalBytes = backups.stream()
                .filter(item -> item.getSizeBytes() != null)
                .mapToLong(BackupView::getSizeBytes)
                .sum();
        lblAlmacenamientoRespaldos.setText(formatBytes(totalBytes));
    }

    /** Cambia el filtro de tipo. */
    private void changeFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    /** Aplica tipo y búsqueda sobre el historial ya cargado. */
    private void applyFilters() {
        String search = txtBuscarRespaldo.getText() == null
                ? ""
                : txtBuscarRespaldo.getText().trim().toLowerCase(Locale.ROOT);
        List<BackupView> filtered = backups.stream()
                .filter(item -> "TODOS".equals(currentFilter)
                        || currentFilter.equals(item.getType()))
                .filter(item -> search.isEmpty()
                        || item.getName().toLowerCase(Locale.ROOT).contains(search)
                        || item.getCreatedBy().toLowerCase(Locale.ROOT).contains(search)
                        || item.getCreatedAt().format(DATE_FORMAT).contains(search))
                .collect(Collectors.toList());
        paintRows(filtered);
        paintSelectedFilter();
    }

    /** Dibuja el historial real. */
    private void paintRows(List<BackupView> filtered) {
        contenedorRespaldos.getChildren().clear();
        if (filtered.isEmpty()) {
            Label empty = new Label("No hay respaldos que coincidan con el filtro.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setStyle("-fx-padding: 28; -fx-alignment: CENTER; -fx-text-fill: #666666;");
            contenedorRespaldos.getChildren().add(empty);
            return;
        }
        filtered.forEach(item -> contenedorRespaldos.getChildren().add(createBackupRow(item)));
    }

    /** Construye una fila y asigna ids únicos a copiar, restaurar y eliminar. */
    private HBox createBackupRow(BackupView item) {
        Label name = createCell("▦  " + item.getName(), 265);
        Label date = createCell(item.getCreatedAt().format(DATE_FORMAT), 170);
        Label type = createCell(formatEnum(item.getType()), 130);
        Label size = createCell(item.getSizeBytes() == null ? "—" : formatBytes(item.getSizeBytes()), 120);
        Label createdBy = createCell(item.getCreatedBy(), 180);

        Label status = new Label(formatEnum(item.getStatus()));
        status.setStyle("-fx-background-color: #d3d3d3; -fx-background-radius: 10; "
                + "-fx-padding: 4 8; -fx-font-size: 8px;");
        StackPane statusCell = new StackPane(status);
        statusCell.setPrefWidth(110);

        Button copy = new Button("Copiar");
        copy.setId("btnCopiarRespaldo" + item.getId());
        copy.setDisable(!"COMPLETADO".equals(item.getStatus()));
        copy.setOnAction(event -> copyBackup(item));
        Button restore = new Button("Restaurar");
        restore.setId("btnRestaurarRespaldo" + item.getId());
        restore.setDisable(!"COMPLETADO".equals(item.getStatus()));
        restore.setOnAction(event -> confirmRestore(item));
        Button delete = new Button("Eliminar");
        delete.setId("btnEliminarRespaldo" + item.getId());
        delete.setOnAction(event -> confirmDelete(item));

        HBox actions = new HBox(6, copy, restore, delete);
        actions.setAlignment(Pos.CENTER);
        HBox.setHgrow(actions, Priority.ALWAYS);

        HBox row = new HBox(name, date, type, size, createdBy, statusCell, actions);
        row.setPrefHeight(55);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent #dedede transparent;");
        return row;
    }

    /** Crea una celda con ancho fijo. */
    private Label createCell(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 9px; -fx-padding: 0 10;");
        return label;
    }

    /** Pide una ruta y crea un archivo .backup real. */
    private void chooseAndCreateBackup() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Crear respaldo PostgreSQL");
        chooser.setInitialFileName("respaldo_utng_"
                + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".backup");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Respaldo PostgreSQL", "*.backup")
        );
        File destination = chooser.showSaveDialog(btnNuevoRespaldo.getScene().getWindow());
        if (destination == null) return;

        runBackground(
                "Creando respaldo",
                () -> backupService.createManualBackup(destination.toPath()),
                "El respaldo se creó correctamente."
        );
    }

    /** Copia un respaldo existente a otra ruta elegida. */
    private void copyBackup(BackupView item) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Copiar respaldo");
        chooser.setInitialFileName(item.getName() + ".backup");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Respaldo PostgreSQL", "*.backup")
        );
        File destination = chooser.showSaveDialog(btnNuevoRespaldo.getScene().getWindow());
        if (destination == null) return;
        try {
            backupService.copyTo(item, destination.toPath());
            showInformation("Respaldo copiado", "El archivo se copió correctamente.");
        } catch (DatabaseException error) {
            showError("No se pudo copiar", error.getMessage());
        }
    }

    /** Solicita confirmación clara antes de reemplazar datos actuales. */
    private void confirmRestore(BackupView item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Restaurar base de datos");
        confirmation.setHeaderText("Esta acción reemplazará los datos actuales");
        confirmation.setContentText("¿Deseas restaurar " + item.getName() + "?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        runBackground(
                "Restaurando base",
                () -> backupService.restore(item),
                "La base de datos se restauró correctamente."
        );
    }

    /** Solicita confirmación antes de borrar el archivo y su historial. */
    private void confirmDelete(BackupView item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Eliminar respaldo");
        confirmation.setHeaderText("Se eliminará el archivo de forma permanente");
        confirmation.setContentText(item.getPath());
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;
        try {
            backupService.delete(item);
            loadBackups();
            showInformation("Respaldo eliminado", "El archivo y su registro fueron eliminados.");
        } catch (DatabaseException error) {
            showError("No se pudo eliminar", error.getMessage());
        }
    }

    /**
     * Ejecuta pg_dump o pg_restore fuera del hilo de interfaz para evitar bloqueos.
     */
    private void runBackground(String title, Runnable operation, String successMessage) {
        btnNuevoRespaldo.setDisable(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                operation.run();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            btnNuevoRespaldo.setDisable(false);
            loadBackups();
            showInformation(title, successMessage);
        });
        task.setOnFailed(event -> {
            btnNuevoRespaldo.setDisable(false);
            Throwable error = task.getException();
            showError(title, error == null ? "Error desconocido." : error.getMessage());
            loadBackups();
        });
        Thread worker = new Thread(task, "utng-postgresql-backup");
        worker.setDaemon(true);
        worker.start();
    }

    /** Convierte bytes a B, KB, MB o GB. */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(Locale.ROOT, "%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format(Locale.ROOT, "%.1f MB", mb);
        return String.format(Locale.ROOT, "%.1f GB", mb / 1024.0);
    }

    /** Convierte enum a texto legible. */
    private String formatEnum(String value) {
        String text = value.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Marca el filtro activo. */
    private void paintSelectedFilter() {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnFiltroTodos.setStyle("TODOS".equals(currentFilter) ? selected : normal);
        btnFiltroAutomaticos.setStyle("AUTOMATICO".equals(currentFilter) ? selected : normal);
        btnFiltroManuales.setStyle("MANUAL".equals(currentFilter) ? selected : normal);
    }

    /** Muestra información. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra errores de herramientas, archivos o PostgreSQL. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

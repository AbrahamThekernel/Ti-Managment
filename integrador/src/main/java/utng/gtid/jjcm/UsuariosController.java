package utng.gtid.jjcm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;
import utng.gtid.jjcm.model.UserView;
import utng.gtid.jjcm.repository.UserRepository;
import utng.gtid.jjcm.security.PasswordHasher;

/**
 * Controlador CRUD de usuarios con filtros y exportación CSV.
 */
public class UsuariosController extends NavigationController {

    /** Formato de fecha visible en la columna de último acceso. */
    private static final DateTimeFormatter ACCESS_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ROOT);

    /** Repositorio que ejecuta el SQL de esta pantalla. */
    private final UserRepository userRepository = new UserRepository();

    /** Último conjunto recuperado de PostgreSQL. */
    private List<UserView> users = new ArrayList<>();

    /** Conjunto actualmente visible; también se utiliza para exportar. */
    private List<UserView> filteredUsers = new ArrayList<>();

    /** Filtro de estado o rol seleccionado. */
    private String currentFilter = "TODOS";

    /** Campo de búsqueda enlazado desde usuarios.fxml. */
    @FXML private TextField txtBuscarUsuario;

    /** Botones normales de filtros enlazados por fx:id. */
    @FXML private Button btnFiltroTodos;
    @FXML private Button btnFiltroActivos;
    @FXML private Button btnFiltroInactivos;
    @FXML private Button btnFiltroAdministradores;

    /** Botones normales de alta y exportación. */
    @FXML private Button btnNuevoUsuario;
    @FXML private Button btnExportarUsuarios;

    /** Etiquetas numéricas calculadas con la consulta real. */
    @FXML private Label lblUsuariosTotales;
    @FXML private Label lblUsuariosActivos;
    @FXML private Label lblAdministradores;
    @FXML private Label lblDocentes;
    @FXML private Label lblUsuariosInactivos;

    /** Contenedor en el que se reemplazan las filas estáticas. */
    @FXML private VBox contenedorUsuarios;

    /** Configura eventos y carga los usuarios al abrir la pantalla. */
    @FXML
    private void initialize() {
        btnFiltroTodos.setOnAction(event -> changeFilter("TODOS"));
        btnFiltroActivos.setOnAction(event -> changeFilter("ACTIVOS"));
        btnFiltroInactivos.setOnAction(event -> changeFilter("INACTIVOS"));
        btnFiltroAdministradores.setOnAction(event -> changeFilter("ADMINISTRADORES"));
        btnNuevoUsuario.setOnAction(event -> showUserDialog(null));
        btnExportarUsuarios.setOnAction(event -> exportUsers());
        txtBuscarUsuario.textProperty().addListener(
                (observable, previous, current) -> applyFilters()
        );
        loadUsers();
    }

    /** Consulta la base y actualiza tabla e indicadores. */
    private void loadUsers() {
        try {
            users = userRepository.findAll();
            updateCounters();
            applyFilters();
        } catch (DatabaseException error) {
            showError("No se pudieron cargar los usuarios", error.getMessage());
        }
    }

    /** Calcula las cinco tarjetas superiores a partir de la lista real. */
    private void updateCounters() {
        lblUsuariosTotales.setText(String.valueOf(users.size()));
        lblUsuariosActivos.setText(String.valueOf(
                users.stream().filter(UserView::isActive).count()
        ));
        lblAdministradores.setText(String.valueOf(
                users.stream().filter(user -> "ADMINISTRADOR".equals(user.getRole())).count()
        ));
        lblDocentes.setText(String.valueOf(
                users.stream().filter(user -> "DOCENTE".equals(user.getRole())).count()
        ));
        lblUsuariosInactivos.setText(String.valueOf(
                users.stream().filter(user -> !user.isActive()).count()
        ));
    }

    /** Cambia el filtro activo. */
    private void changeFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    /** Aplica el filtro seleccionado y la búsqueda escrita. */
    private void applyFilters() {
        String search = txtBuscarUsuario.getText() == null
                ? ""
                : txtBuscarUsuario.getText().trim().toLowerCase(Locale.ROOT);

        filteredUsers = users.stream()
                .filter(this::matchesFilter)
                .filter(user -> search.isEmpty()
                        || user.getFullName().toLowerCase(Locale.ROOT).contains(search)
                        || user.getEmail().toLowerCase(Locale.ROOT).contains(search)
                        || user.getDepartment().toLowerCase(Locale.ROOT).contains(search))
                .collect(Collectors.toList());

        paintRows();
        paintSelectedFilter();
    }

    /** Comprueba el estado o rol contra el filtro actual. */
    private boolean matchesFilter(UserView user) {
        if ("ACTIVOS".equals(currentFilter)) {
            return user.isActive();
        }
        if ("INACTIVOS".equals(currentFilter)) {
            return !user.isActive();
        }
        if ("ADMINISTRADORES".equals(currentFilter)) {
            return "ADMINISTRADOR".equals(user.getRole());
        }
        return true;
    }

    /** Limpia la maqueta y crea una fila por cada usuario filtrado. */
    private void paintRows() {
        contenedorUsuarios.getChildren().clear();
        if (filteredUsers.isEmpty()) {
            Label empty = new Label("No hay usuarios que coincidan con el filtro.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setStyle("-fx-padding: 28; -fx-alignment: CENTER; -fx-text-fill: #666666;");
            contenedorUsuarios.getChildren().add(empty);
            return;
        }
        filteredUsers.forEach(user -> contenedorUsuarios.getChildren().add(createUserRow(user)));
    }

    /** Construye una fila y sus botones normales con ids únicos. */
    private HBox createUserRow(UserView user) {
        Label name = createCell("◯  " + user.getFullName(), 210, true);
        Label email = createCell(user.getEmail(), 220, false);
        Label role = createCell(formatEnum(user.getRole()), 135, false);
        Label department = createCell(user.getDepartment(), 175, false);

        Label status = new Label(user.isActive() ? "Activo" : "Inactivo");
        status.setStyle("-fx-background-color: #d3d3d3; -fx-background-radius: 10; "
                + "-fx-padding: 4 11; -fx-font-size: 8px;");
        StackPane statusCell = new StackPane(status);
        statusCell.setPrefWidth(115);

        String accessText = user.getLastAccess() == null
                ? "Sin acceso"
                : user.getLastAccess().format(ACCESS_FORMAT);
        Label access = createCell(accessText, 145, false);

        Button editButton = new Button("Editar");
        editButton.setId("btnEditarUsuario" + user.getId());
        editButton.setOnAction(event -> showUserDialog(user));

        Button toggleButton = new Button(user.isActive() ? "Desactivar" : "Activar");
        toggleButton.setId("btnCambiarEstadoUsuario" + user.getId());
        toggleButton.setOnAction(event -> toggleUser(user));

        HBox actions = new HBox(7, editButton, toggleButton);
        actions.setAlignment(Pos.CENTER);
        HBox.setHgrow(actions, Priority.ALWAYS);

        HBox row = new HBox(name, email, role, department, statusCell, access, actions);
        row.setPrefHeight(56);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent #dedede transparent;");
        return row;
    }

    /** Crea una celda de la tabla con ancho y peso configurables. */
    private Label createCell(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 9px; -fx-padding: 0 8;"
                + (bold ? " -fx-font-weight: bold;" : ""));
        return label;
    }

    /**
     * Abre un formulario reutilizable para INSERT o UPDATE.
     */
    private void showUserDialog(UserView user) {
        try {
            List<CatalogItem> roles = userRepository.findActiveRoles();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(user == null ? "Nuevo usuario" : "Editar usuario");
            dialog.setHeaderText("Información institucional del usuario");

            ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            ComboBox<CatalogItem> roleBox = new ComboBox<>(FXCollections.observableArrayList(roles));
            roleBox.setId("cmbRolUsuario");
            roleBox.setMaxWidth(Double.MAX_VALUE);
            TextField name = createField("txtNombreUsuario", "Nombre");
            TextField lastName = createField("txtApellidosUsuario", "Apellidos");
            TextField email = createField("txtCorreoUsuario", "correo@utng.edu.mx");
            TextField phone = createField("txtTelefonoUsuario", "Teléfono opcional");
            TextField position = createField("txtPuestoUsuario", "Puesto opcional");
            TextField department = createField("txtDepartamentoUsuario", "Departamento opcional");
            PasswordField password = new PasswordField();
            password.setId("txtContrasenaUsuario");
            password.setPromptText(user == null ? "Contraseña inicial opcional" : "No se modifica aquí");
            password.setDisable(user != null);

            fillExistingUser(user, roles, roleBox, name, lastName, email,
                    phone, position, department);

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(9);
            form.setPadding(new Insets(10));
            form.addRow(0, new Label("Rol:"), roleBox);
            form.addRow(1, new Label("Nombre:"), name);
            form.addRow(2, new Label("Apellidos:"), lastName);
            form.addRow(3, new Label("Correo:"), email);
            form.addRow(4, new Label("Teléfono:"), phone);
            form.addRow(5, new Label("Puesto:"), position);
            form.addRow(6, new Label("Departamento:"), department);
            form.addRow(7, new Label("Contraseña:"), password);
            dialog.getDialogPane().setContent(form);

            boolean saved = false;
            while (!saved) {
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isEmpty() || result.get() != saveType) {
                    return;
                }
                try {
                    saveUser(user, roleBox.getValue(), name.getText(), lastName.getText(),
                            email.getText(), phone.getText(), position.getText(),
                            department.getText(), password.getText());
                    saved = true;
                } catch (IllegalArgumentException error) {
                    showError("Datos incorrectos", error.getMessage());
                }
            }
            loadUsers();
        } catch (DatabaseException error) {
            showError("No se pudo guardar el usuario", error.getMessage());
        }
    }

    /** Crea un TextField normal con un id explícito. */
    private TextField createField(String id, String prompt) {
        TextField field = new TextField();
        field.setId(id);
        field.setPromptText(prompt);
        field.setPrefWidth(280);
        return field;
    }

    /** Llena el formulario cuando la operación es una edición. */
    private void fillExistingUser(
            UserView user,
            List<CatalogItem> roles,
            ComboBox<CatalogItem> roleBox,
            TextField name,
            TextField lastName,
            TextField email,
            TextField phone,
            TextField position,
            TextField department
    ) {
        if (user == null) {
            if (!roles.isEmpty()) {
                roleBox.getSelectionModel().selectFirst();
            }
            return;
        }

        roles.stream().filter(role -> role.getId() == user.getRoleId()).findFirst()
                .ifPresent(roleBox.getSelectionModel()::select);
        name.setText(user.getName());
        lastName.setText(user.getLastName());
        email.setText(user.getEmail());
        phone.setText(user.getPhone());
        position.setText(user.getPosition());
        department.setText(user.getDepartment());
    }

    /** Valida datos y decide si se crea o actualiza el usuario. */
    private void saveUser(
            UserView user,
            CatalogItem role,
            String name,
            String lastName,
            String email,
            String phone,
            String position,
            String department,
            String password
    ) {
        if (role == null) {
            throw new IllegalArgumentException("Selecciona un rol.");
        }
        if (name == null || name.isBlank() || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Nombre y apellidos son obligatorios.");
        }
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Escribe un correo válido.");
        }

        if (user == null) {
            String hash = password == null || password.isBlank()
                    ? null
                    : PasswordHasher.hash(password);
            userRepository.insert(role.getId(), name.trim(), lastName.trim(),
                    email.trim().toLowerCase(Locale.ROOT), phone, position, department, hash);
        } else {
            userRepository.update(user.getId(), role.getId(), name.trim(), lastName.trim(),
                    email.trim().toLowerCase(Locale.ROOT), phone, position, department);
        }
    }

    /** Alterna la baja lógica del usuario. */
    private void toggleUser(UserView user) {
        try {
            userRepository.toggleActive(user.getId());
            loadUsers();
        } catch (DatabaseException error) {
            showError("No se pudo cambiar el estado", error.getMessage());
        }
    }

    /** Exporta la lista visible como CSV compatible con Excel. */
    private void exportUsers() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar usuarios");
        chooser.setInitialFileName("usuarios_utng.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV", "*.csv"));
        File file = chooser.showSaveDialog(btnExportarUsuarios.getScene().getWindow());
        if (file == null) {
            return;
        }

        StringBuilder csv = new StringBuilder("Nombre,Apellidos,Correo,Rol,Departamento,Estado\n");
        for (UserView user : filteredUsers) {
            csv.append(csv(user.getName())).append(',')
                    .append(csv(user.getLastName())).append(',')
                    .append(csv(user.getEmail())).append(',')
                    .append(csv(user.getRole())).append(',')
                    .append(csv(user.getDepartment())).append(',')
                    .append(csv(user.isActive() ? "Activo" : "Inactivo")).append('\n');
        }

        try {
            // La marca UTF-8 ayuda a que Excel reconozca correctamente los acentos.
            Files.writeString(file.toPath(), "\uFEFF" + csv, StandardCharsets.UTF_8);
            showInformation("Exportación terminada", "El archivo se guardó correctamente.");
        } catch (IOException error) {
            showError("No se pudo exportar", error.getMessage());
        }
    }

    /** Escapa comillas y comas según el formato CSV. */
    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    /** Convierte un valor ENUMERO_CON_GUION a texto legible. */
    private String formatEnum(String value) {
        String lower = value.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    /** Marca el botón que representa el filtro actual. */
    private void paintSelectedFilter() {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnFiltroTodos.setStyle("TODOS".equals(currentFilter) ? selected : normal);
        btnFiltroActivos.setStyle("ACTIVOS".equals(currentFilter) ? selected : normal);
        btnFiltroInactivos.setStyle("INACTIVOS".equals(currentFilter) ? selected : normal);
        btnFiltroAdministradores.setStyle(
                "ADMINISTRADORES".equals(currentFilter) ? selected : normal
        );
    }

    /** Muestra una confirmación informativa. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra errores de validación, archivos o PostgreSQL. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

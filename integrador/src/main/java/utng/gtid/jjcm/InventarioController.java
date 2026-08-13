package utng.gtid.jjcm;

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
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;
import utng.gtid.jjcm.model.ProductView;
import utng.gtid.jjcm.repository.CatalogRepository;
import utng.gtid.jjcm.repository.ProductRepository;

/**
 * Controlador CRUD del inventario de productos prestables.
 *
 * <p>CRUD significa crear, consultar, actualizar y dar de baja. La baja es
 * lógica: el registro se conserva para no romper el historial de préstamos.</p>
 */
public class InventarioController extends NavigationController {

    /** Repositorio responsable de las operaciones SQL de productos. */
    private final ProductRepository productRepository = new ProductRepository();

    /** Repositorio utilizado para llenar el catálogo de categorías. */
    private final CatalogRepository catalogRepository = new CatalogRepository();

    /** Copia local de la última consulta, utilizada por búsqueda y filtros. */
    private List<ProductView> products = new ArrayList<>();

    /** Filtro actual: TODOS, ACTIVOS o BAJA. */
    private String currentFilter = "TODOS";

    /** Campo FXML que permite buscar por producto, modelo o categoría. */
    @FXML
    private TextField txtBuscarProducto;

    /** Botón normal que elimina el filtro de estado. */
    @FXML
    private Button btnFiltroTodos;

    /** Botón normal que muestra únicamente registros activos. */
    @FXML
    private Button btnFiltroActivos;

    /** Botón normal que muestra registros dados de baja. */
    @FXML
    private Button btnFiltroBaja;

    /** Botón normal que abre el formulario para registrar un producto. */
    @FXML
    private Button btnDarAlta;

    /** Contenedor FXML en el que se construyen las filas provenientes de BD. */
    @FXML
    private VBox inventoryRows;

    /**
     * Configura todos los eventos al terminar de cargar el archivo FXML.
     */
    @FXML
    private void initialize() {
        btnFiltroTodos.setOnAction(event -> changeFilter("TODOS"));
        btnFiltroActivos.setOnAction(event -> changeFilter("ACTIVOS"));
        btnFiltroBaja.setOnAction(event -> changeFilter("BAJA"));
        btnDarAlta.setOnAction(event -> showProductDialog(null));

        // Cada cambio de texto vuelve a aplicar el filtro sin consultar de más.
        txtBuscarProducto.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        loadProducts();
    }

    /**
     * Consulta todos los productos una vez y vuelve a dibujar la tabla.
     */
    private void loadProducts() {
        try {
            products = productRepository.findAll();
            applyFilters();
        } catch (DatabaseException error) {
            showError("No se pudo cargar el inventario", error.getMessage());
        }
    }

    /**
     * Cambia el estado del filtro seleccionado y actualiza la tabla.
     */
    private void changeFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    /**
     * Aplica simultáneamente el estado y el texto escrito por el usuario.
     */
    private void applyFilters() {
        String search = txtBuscarProducto.getText() == null
                ? ""
                : txtBuscarProducto.getText().trim().toLowerCase(Locale.ROOT);

        List<ProductView> filtered = products.stream()
                .filter(product -> matchesState(product) && matchesText(product, search))
                .collect(Collectors.toList());

        paintRows(filtered);
        paintSelectedFilter();
    }

    /** Devuelve true cuando el producto cumple el filtro de estado actual. */
    private boolean matchesState(ProductView product) {
        if ("ACTIVOS".equals(currentFilter)) {
            return product.isActive();
        }
        if ("BAJA".equals(currentFilter)) {
            return !product.isActive();
        }
        return true;
    }

    /** Devuelve true cuando producto, modelo o categoría contienen el texto. */
    private boolean matchesText(ProductView product, String search) {
        return search.isEmpty()
                || product.getName().toLowerCase(Locale.ROOT).contains(search)
                || product.getModel().toLowerCase(Locale.ROOT).contains(search)
                || product.getCategory().toLowerCase(Locale.ROOT).contains(search);
    }

    /**
     * Sustituye el contenido anterior con filas construidas desde PostgreSQL.
     */
    private void paintRows(List<ProductView> filteredProducts) {
        inventoryRows.getChildren().clear();

        if (filteredProducts.isEmpty()) {
            Label empty = new Label("No hay productos que coincidan con el filtro.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setStyle("-fx-padding: 30; -fx-alignment: CENTER; -fx-text-fill: #666666;");
            inventoryRows.getChildren().add(empty);
            return;
        }

        for (ProductView product : filteredProducts) {
            inventoryRows.getChildren().add(createProductRow(product));
        }
    }

    /**
     * Construye una fila visual y asigna ids únicos a sus botones de acción.
     */
    private HBox createProductRow(ProductView product) {
        StackPane image = new StackPane(new Label("◇"));
        image.setPrefSize(34, 34);
        image.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #cccccc;");

        Label productName = new Label(product.getName());
        productName.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        Label minimumStock = new Label("Mínimo: " + product.getMinimumStock());
        minimumStock.setStyle("-fx-font-size: 8px; -fx-text-fill: #666666;");

        VBox productText = new VBox(3, productName, minimumStock);
        HBox productCell = new HBox(10, image, productText);
        productCell.setPrefWidth(240);
        productCell.setAlignment(Pos.CENTER_LEFT);
        productCell.setPadding(new Insets(8, 12, 8, 18));

        Label category = createCell(product.getCategory(), 190);
        Label model = createCell(product.getModel().isBlank() ? "Sin modelo" : product.getModel(), 180);
        Label stock = createCell(String.valueOf(product.getStock()), 125);

        Label status = new Label(product.isActive() ? "Activo" : "Baja");
        status.setStyle("-fx-background-color: #d2d2d2; -fx-background-radius: 10; "
                + "-fx-padding: 3 12; -fx-font-size: 9px;");
        StackPane statusCell = new StackPane(status);
        statusCell.setPrefWidth(150);

        Button editButton = new Button("Editar");
        editButton.setId("btnEditarProducto" + product.getId());
        editButton.setOnAction(event -> showProductDialog(product));

        Button toggleButton = new Button(product.isActive() ? "Dar de baja" : "Activar");
        toggleButton.setId("btnCambiarEstadoProducto" + product.getId());
        toggleButton.setOnAction(event -> toggleProduct(product));

        HBox actions = new HBox(8, editButton, toggleButton);
        actions.setAlignment(Pos.CENTER);
        HBox.setHgrow(actions, Priority.ALWAYS);

        HBox row = new HBox(productCell, category, model, stock, statusCell, actions);
        row.setPrefHeight(58);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent #dddddd transparent;");
        return row;
    }

    /** Crea una celda de texto centrada con una medida compatible con FXML. */
    private Label createCell(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-size: 9px; -fx-border-color: transparent #dddddd transparent transparent;");
        return label;
    }

    /**
     * Abre el mismo formulario para alta o edición y guarda el resultado.
     * Cuando product es null se realiza un INSERT; en otro caso un UPDATE.
     */
    private void showProductDialog(ProductView product) {
        try {
            List<CatalogItem> categories = catalogRepository.findActiveCategories();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(product == null ? "Nuevo producto" : "Editar producto");
            dialog.setHeaderText("Completa los datos del inventario");

            ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            ComboBox<CatalogItem> categoryBox = new ComboBox<>(
                    FXCollections.observableArrayList(categories)
            );
            categoryBox.setId("cmbCategoriaProducto");
            categoryBox.setMaxWidth(Double.MAX_VALUE);

            TextField nameField = createField("txtNombreProducto", "Nombre");
            TextField modelField = createField("txtModeloProducto", "Modelo opcional");
            TextField stockField = createField("txtStockProducto", "Stock");
            TextField minimumField = createField("txtStockMinimoProducto", "Stock mínimo");

            fillExistingProduct(product, categories, categoryBox, nameField,
                    modelField, stockField, minimumField);

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(10);
            form.setPadding(new Insets(10));
            form.addRow(0, new Label("Categoría:"), categoryBox);
            form.addRow(1, new Label("Nombre:"), nameField);
            form.addRow(2, new Label("Modelo:"), modelField);
            form.addRow(3, new Label("Stock:"), stockField);
            form.addRow(4, new Label("Stock mínimo:"), minimumField);
            GridPane.setHgrow(categoryBox, Priority.ALWAYS);
            GridPane.setHgrow(nameField, Priority.ALWAYS);
            dialog.getDialogPane().setContent(form);

            // El ciclo conserva los datos cuando una validación falla.
            boolean saved = false;
            while (!saved) {
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isEmpty() || result.get() != saveType) {
                    return;
                }
                try {
                    saveProduct(product, categoryBox.getValue(), nameField.getText(),
                            modelField.getText(), stockField.getText(), minimumField.getText());
                    saved = true;
                } catch (IllegalArgumentException error) {
                    showError("Datos incorrectos", error.getMessage());
                }
            }

            loadProducts();
        } catch (DatabaseException error) {
            showError("No se pudo guardar el producto", error.getMessage());
        }
    }

    /** Crea un TextField normal con id explícito para poder localizarlo. */
    private TextField createField(String id, String prompt) {
        TextField field = new TextField();
        field.setId(id);
        field.setPromptText(prompt);
        field.setPrefWidth(260);
        return field;
    }

    /** Coloca en el formulario los datos existentes cuando se está editando. */
    private void fillExistingProduct(
            ProductView product,
            List<CatalogItem> categories,
            ComboBox<CatalogItem> categoryBox,
            TextField nameField,
            TextField modelField,
            TextField stockField,
            TextField minimumField
    ) {
        if (product == null) {
            if (!categories.isEmpty()) {
                categoryBox.getSelectionModel().selectFirst();
            }
            stockField.setText("0");
            minimumField.setText("0");
            return;
        }

        categories.stream()
                .filter(category -> category.getId() == product.getCategoryId())
                .findFirst()
                .ifPresent(categoryBox.getSelectionModel()::select);
        nameField.setText(product.getName());
        modelField.setText(product.getModel());
        stockField.setText(String.valueOf(product.getStock()));
        minimumField.setText(String.valueOf(product.getMinimumStock()));
    }

    /** Valida el formulario y ejecuta INSERT o UPDATE según corresponda. */
    private void saveProduct(
            ProductView product,
            CatalogItem category,
            String name,
            String model,
            String stockText,
            String minimumText
    ) {
        if (category == null) {
            throw new IllegalArgumentException("Selecciona una categoría.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre del producto.");
        }

        int stock = parseNonNegative(stockText, "stock");
        int minimumStock = parseNonNegative(minimumText, "stock mínimo");

        if (product == null) {
            productRepository.insert(category.getId(), name.trim(), model, stock, minimumStock);
        } else {
            productRepository.update(product.getId(), category.getId(), name.trim(),
                    model, stock, minimumStock);
        }
    }

    /** Convierte un texto a entero y rechaza valores vacíos, letras o negativos. */
    private int parseNonNegative(String value, String fieldName) {
        try {
            int number = Integer.parseInt(value == null ? "" : value.trim());
            if (number < 0) {
                throw new NumberFormatException("negative");
            }
            return number;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                    "El campo " + fieldName + " debe ser un número entero igual o mayor que cero."
            );
        }
    }

    /** Activa o da de baja el registro seleccionado y refresca la pantalla. */
    private void toggleProduct(ProductView product) {
        try {
            productRepository.toggleActive(product.getId());
            loadProducts();
        } catch (DatabaseException error) {
            showError("No se pudo cambiar el estado", error.getMessage());
        }
    }

    /** Marca visualmente cuál de los tres botones de filtro está activo. */
    private void paintSelectedFilter() {
        String normal = "-fx-background-color: white; -fx-border-color: #d0d0d0;";
        String selected = "-fx-background-color: #e8e8e8; -fx-border-color: #d0d0d0; "
                + "-fx-font-weight: bold;";
        btnFiltroTodos.setStyle("TODOS".equals(currentFilter) ? selected : normal);
        btnFiltroActivos.setStyle("ACTIVOS".equals(currentFilter) ? selected : normal);
        btnFiltroBaja.setStyle("BAJA".equals(currentFilter) ? selected : normal);
    }

    /** Muestra al usuario los errores de validación o persistencia. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

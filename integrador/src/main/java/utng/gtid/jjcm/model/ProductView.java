package utng.gtid.jjcm.model;

/**
 * Producto de inventario listo para mostrarse o editarse.
 */
public final class ProductView {

    /** Llave primaria del producto. */
    private final long id;

    /** Llave de su categoría. */
    private final long categoryId;

    /** Nombre de la categoría. */
    private final String category;

    /** Nombre comercial del producto. */
    private final String name;

    /** Modelo o referencia del fabricante. */
    private final String model;

    /** Existencias disponibles para préstamo. */
    private final int stock;

    /** Nivel a partir del cual se debe reabastecer. */
    private final int minimumStock;

    /** Indica si el producto puede utilizarse. */
    private final boolean active;

    /** Construye una fila inmutable de inventario. */
    public ProductView(
            long id,
            long categoryId,
            String category,
            String name,
            String model,
            int stock,
            int minimumStock,
            boolean active
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.category = category;
        this.name = name;
        this.model = model;
        this.stock = stock;
        this.minimumStock = minimumStock;
        this.active = active;
    }

    /** @return id del producto. */
    public long getId() { return id; }

    /** @return id de categoría. */
    public long getCategoryId() { return categoryId; }

    /** @return nombre de categoría. */
    public String getCategory() { return category; }

    /** @return nombre del producto. */
    public String getName() { return name; }

    /** @return modelo. */
    public String getModel() { return model; }

    /** @return stock actual. */
    public int getStock() { return stock; }

    /** @return stock mínimo. */
    public int getMinimumStock() { return minimumStock; }

    /** @return true cuando está activo. */
    public boolean isActive() { return active; }
}

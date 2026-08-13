package utng.gtid.jjcm.model;

/**
 * Elemento reutilizable para ComboBox que conserva el identificador de BD.
 */
public final class CatalogItem {

    /** Llave primaria del registro PostgreSQL. */
    private final long id;

    /** Texto principal visible dentro del ComboBox. */
    private final String name;

    /** Texto secundario, como departamento o modelo. */
    private final String detail;

    /** Cantidad disponible; vale cero cuando el catálogo no maneja stock. */
    private final int available;

    /** Construye un elemento inmutable de catálogo. */
    public CatalogItem(long id, String name, String detail, int available) {
        this.id = id;
        this.name = name;
        this.detail = detail;
        this.available = available;
    }

    /** @return llave primaria del registro. */
    public long getId() {
        return id;
    }

    /** @return nombre visible. */
    public String getName() {
        return name;
    }

    /** @return información complementaria. */
    public String getDetail() {
        return detail;
    }

    /** @return unidades actualmente disponibles. */
    public int getAvailable() {
        return available;
    }

    /**
     * ComboBox usa automáticamente toString para pintar cada opción.
     */
    @Override
    public String toString() {
        return name;
    }
}

package utng.gtid.jjcm.model;

/**
 * Equipo individual identificado por código y número de serie.
 */
public final class EquipmentView {

    /** Identificadores del equipo y de sus catálogos relacionados. */
    private final long id;
    private final long categoryId;
    private final Long locationId;

    /** Datos visibles y editables del activo. */
    private final String code;
    private final String name;
    private final String category;
    private final String model;
    private final String serialNumber;
    private final String status;
    private final String location;
    private final String observations;

    /** Construye una vista inmutable a partir de una fila SQL. */
    public EquipmentView(
            long id,
            long categoryId,
            Long locationId,
            String code,
            String name,
            String category,
            String model,
            String serialNumber,
            String status,
            String location,
            String observations
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.locationId = locationId;
        this.code = code;
        this.name = name;
        this.category = category;
        this.model = model;
        this.serialNumber = serialNumber;
        this.status = status;
        this.location = location;
        this.observations = observations;
    }

    /** @return id del equipo. */
    public long getId() { return id; }

    /** @return id de categoría. */
    public long getCategoryId() { return categoryId; }

    /** @return id de ubicación o null. */
    public Long getLocationId() { return locationId; }

    /** @return código institucional. */
    public String getCode() { return code; }

    /** @return nombre del equipo. */
    public String getName() { return name; }

    /** @return categoría visible. */
    public String getCategory() { return category; }

    /** @return modelo. */
    public String getModel() { return model; }

    /** @return número de serie. */
    public String getSerialNumber() { return serialNumber; }

    /** @return estado PostgreSQL. */
    public String getStatus() { return status; }

    /** @return ubicación visible. */
    public String getLocation() { return location; }

    /** @return observaciones. */
    public String getObservations() { return observations; }
}

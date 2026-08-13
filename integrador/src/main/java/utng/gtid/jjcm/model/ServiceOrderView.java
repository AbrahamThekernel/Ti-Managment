package utng.gtid.jjcm.model;

import java.time.LocalDateTime;

/**
 * Orden de servicio lista para mostrarse en su tabla.
 */
public final class ServiceOrderView {

    /** Identificadores necesarios para relaciones y cambios de estado. */
    private final long id;
    private final long equipmentId;

    /** Datos visibles de la orden. */
    private final String folio;
    private final LocalDateTime requestDate;
    private final String equipment;
    private final String requester;
    private final String responsible;
    private final String serviceType;
    private final String description;
    private final String status;
    private final String priority;

    /** Construye una orden inmutable a partir de una consulta JOIN. */
    public ServiceOrderView(
            long id,
            long equipmentId,
            String folio,
            LocalDateTime requestDate,
            String equipment,
            String requester,
            String responsible,
            String serviceType,
            String description,
            String status,
            String priority
    ) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.folio = folio;
        this.requestDate = requestDate;
        this.equipment = equipment;
        this.requester = requester;
        this.responsible = responsible;
        this.serviceType = serviceType;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }

    /** @return id de orden. */
    public long getId() { return id; }

    /** @return id de equipo relacionado. */
    public long getEquipmentId() { return equipmentId; }

    /** @return folio institucional. */
    public String getFolio() { return folio; }

    /** @return fecha de solicitud. */
    public LocalDateTime getRequestDate() { return requestDate; }

    /** @return equipo visible. */
    public String getEquipment() { return equipment; }

    /** @return solicitante visible. */
    public String getRequester() { return requester; }

    /** @return responsable visible. */
    public String getResponsible() { return responsible; }

    /** @return tipo de servicio. */
    public String getServiceType() { return serviceType; }

    /** @return descripción completa. */
    public String getDescription() { return description; }

    /** @return estado de base de datos. */
    public String getStatus() { return status; }

    /** @return prioridad. */
    public String getPriority() { return priority; }
}

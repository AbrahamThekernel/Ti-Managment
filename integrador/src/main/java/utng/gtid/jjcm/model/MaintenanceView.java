package utng.gtid.jjcm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mantenimiento preventivo o correctivo listo para la interfaz.
 */
public final class MaintenanceView {

    /** Identificadores de mantenimiento y equipo relacionado. */
    private final long id;
    private final long equipmentId;

    /** Datos visibles y operativos. */
    private final String folio;
    private final LocalDate scheduledDate;
    private final String equipment;
    private final String type;
    private final String technician;
    private final String status;
    private final String priority;
    private final String diagnosis;
    private final String completedWork;
    private final BigDecimal cost;

    /** Construye una fila inmutable de mantenimiento. */
    public MaintenanceView(
            long id,
            long equipmentId,
            String folio,
            LocalDate scheduledDate,
            String equipment,
            String type,
            String technician,
            String status,
            String priority,
            String diagnosis,
            String completedWork,
            BigDecimal cost
    ) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.folio = folio;
        this.scheduledDate = scheduledDate;
        this.equipment = equipment;
        this.type = type;
        this.technician = technician;
        this.status = status;
        this.priority = priority;
        this.diagnosis = diagnosis;
        this.completedWork = completedWork;
        this.cost = cost;
    }

    /** @return id del mantenimiento. */
    public long getId() { return id; }

    /** @return id del equipo. */
    public long getEquipmentId() { return equipmentId; }

    /** @return folio. */
    public String getFolio() { return folio; }

    /** @return fecha programada. */
    public LocalDate getScheduledDate() { return scheduledDate; }

    /** @return equipo visible. */
    public String getEquipment() { return equipment; }

    /** @return tipo preventivo o correctivo. */
    public String getType() { return type; }

    /** @return técnico visible. */
    public String getTechnician() { return technician; }

    /** @return estado. */
    public String getStatus() { return status; }

    /** @return prioridad. */
    public String getPriority() { return priority; }

    /** @return diagnóstico. */
    public String getDiagnosis() { return diagnosis; }

    /** @return trabajo realizado. */
    public String getCompletedWork() { return completedWork; }

    /** @return costo. */
    public BigDecimal getCost() { return cost; }
}

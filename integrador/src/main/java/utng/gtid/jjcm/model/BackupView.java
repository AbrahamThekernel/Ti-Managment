package utng.gtid.jjcm.model;

import java.time.LocalDateTime;

/**
 * Metadatos de un archivo de respaldo PostgreSQL.
 */
public final class BackupView {

    /** Identificador y atributos persistidos en la tabla respaldos. */
    private final long id;
    private final String name;
    private final String path;
    private final String type;
    private final String status;
    private final Long sizeBytes;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final String message;

    /** Construye un registro de respaldo inmutable. */
    public BackupView(long id, String name, String path, String type, String status,
                      Long sizeBytes, String createdBy, LocalDateTime createdAt,
                      String message) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.type = type;
        this.status = status;
        this.sizeBytes = sizeBytes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.message = message;
    }

    /** @return id. */ public long getId() { return id; }
    /** @return nombre. */ public String getName() { return name; }
    /** @return ruta absoluta. */ public String getPath() { return path; }
    /** @return tipo MANUAL o AUTOMATICO. */ public String getType() { return type; }
    /** @return estado. */ public String getStatus() { return status; }
    /** @return tamaño o null. */ public Long getSizeBytes() { return sizeBytes; }
    /** @return creador visible. */ public String getCreatedBy() { return createdBy; }
    /** @return fecha y hora. */ public LocalDateTime getCreatedAt() { return createdAt; }
    /** @return mensaje del proceso. */ public String getMessage() { return message; }
}

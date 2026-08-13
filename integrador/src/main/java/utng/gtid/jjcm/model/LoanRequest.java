package utng.gtid.jjcm.model;

import java.time.LocalDate;

/**
 * Datos necesarios para crear un préstamo dentro de una transacción.
 */
public final class LoanRequest {

    /** Usuario que recibe el producto. */
    private final long borrowerId;

    /** Producto prestado. */
    private final long productId;

    /** Usuario administrador que captura el movimiento. */
    private final long registeredById;

    /** Número de unidades solicitadas. */
    private final int quantity;

    /** Fecha en que se entrega el producto. */
    private final LocalDate loanDate;

    /** Fecha acordada para devolverlo. */
    private final LocalDate dueDate;

    /** Motivo institucional del préstamo. */
    private final String reason;

    /** Notas adicionales proporcionadas por el administrador. */
    private final String notes;

    /** Indica que el solicitante aceptó la responsiva. */
    private final boolean responsibilityAccepted;

    /** Construye una solicitud inmutable validada por el servicio. */
    public LoanRequest(
            long borrowerId,
            long productId,
            long registeredById,
            int quantity,
            LocalDate loanDate,
            LocalDate dueDate,
            String reason,
            String notes,
            boolean responsibilityAccepted
    ) {
        this.borrowerId = borrowerId;
        this.productId = productId;
        this.registeredById = registeredById;
        this.quantity = quantity;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.reason = reason;
        this.notes = notes;
        this.responsibilityAccepted = responsibilityAccepted;
    }

    /** @return identificador del solicitante. */
    public long getBorrowerId() { return borrowerId; }

    /** @return identificador del producto. */
    public long getProductId() { return productId; }

    /** @return identificador del administrador que registra. */
    public long getRegisteredById() { return registeredById; }

    /** @return cantidad solicitada. */
    public int getQuantity() { return quantity; }

    /** @return fecha inicial. */
    public LocalDate getLoanDate() { return loanDate; }

    /** @return fecha de devolución programada. */
    public LocalDate getDueDate() { return dueDate; }

    /** @return motivo del préstamo. */
    public String getReason() { return reason; }

    /** @return observaciones adicionales. */
    public String getNotes() { return notes; }

    /** @return true cuando se aceptó la responsiva. */
    public boolean isResponsibilityAccepted() { return responsibilityAccepted; }
}

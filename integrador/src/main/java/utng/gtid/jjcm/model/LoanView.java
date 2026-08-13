package utng.gtid.jjcm.model;

import java.time.LocalDate;

/**
 * Proyección lista para mostrar un préstamo en la interfaz.
 */
public final class LoanView {

    /** Identificador del encabezado del préstamo. */
    private final long loanId;

    /** Identificador del detalle utilizado para registrar devoluciones. */
    private final long detailId;

    /** Folio legible del movimiento. */
    private final String folio;

    /** Nombre completo del solicitante. */
    private final String borrower;

    /** Nombre del producto prestado. */
    private final String product;

    /** Cantidad total registrada. */
    private final int quantity;

    /** Cantidad que aún no ha regresado al inventario. */
    private final int pendingQuantity;

    /** Fecha de entrega. */
    private final LocalDate loanDate;

    /** Fecha límite de devolución. */
    private final LocalDate dueDate;

    /** Estado ACTIVO, DEVUELTO, VENCIDO o CANCELADO. */
    private final String status;

    /** Construye una fila inmutable del listado. */
    public LoanView(
            long loanId,
            long detailId,
            String folio,
            String borrower,
            String product,
            int quantity,
            int pendingQuantity,
            LocalDate loanDate,
            LocalDate dueDate,
            String status
    ) {
        this.loanId = loanId;
        this.detailId = detailId;
        this.folio = folio;
        this.borrower = borrower;
        this.product = product;
        this.quantity = quantity;
        this.pendingQuantity = pendingQuantity;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    /** @return id del préstamo. */
    public long getLoanId() { return loanId; }

    /** @return id del detalle. */
    public long getDetailId() { return detailId; }

    /** @return folio público. */
    public String getFolio() { return folio; }

    /** @return solicitante. */
    public String getBorrower() { return borrower; }

    /** @return producto. */
    public String getProduct() { return product; }

    /** @return cantidad inicial. */
    public int getQuantity() { return quantity; }

    /** @return cantidad pendiente. */
    public int getPendingQuantity() { return pendingQuantity; }

    /** @return fecha de préstamo. */
    public LocalDate getLoanDate() { return loanDate; }

    /** @return fecha programada de devolución. */
    public LocalDate getDueDate() { return dueDate; }

    /** @return estado persistido. */
    public String getStatus() { return status; }
}

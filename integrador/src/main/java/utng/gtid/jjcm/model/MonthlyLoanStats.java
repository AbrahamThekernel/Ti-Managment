package utng.gtid.jjcm.model;

import java.time.LocalDate;

/**
 * Representa las cantidades mensuales utilizadas por la gráfica del panel.
 * La clase transporta datos y no ejecuta consultas por sí misma.
 */
public final class MonthlyLoanStats {

    /** Primer día del mes devuelto por PostgreSQL. */
    private final LocalDate month;

    /** Préstamos cuya fecha de préstamo pertenece al mes. */
    private final int loans;

    /** Préstamos cuya devolución real pertenece al mes. */
    private final int returns;

    /** Construye una fila inmutable de la gráfica. */
    public MonthlyLoanStats(LocalDate month, int loans, int returns) {
        this.month = month;
        this.loans = loans;
        this.returns = returns;
    }

    /** @return primer día del mes representado. */
    public LocalDate getMonth() {
        return month;
    }

    /** @return cantidad de préstamos iniciados durante el mes. */
    public int getLoans() {
        return loans;
    }

    /** @return cantidad de devoluciones registradas durante el mes. */
    public int getReturns() {
        return returns;
    }
}

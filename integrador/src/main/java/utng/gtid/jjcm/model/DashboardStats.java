package utng.gtid.jjcm.model;

/**
 * Totales calculados por PostgreSQL para el panel de estadísticas.
 */
public final class DashboardStats {

    /** Número total de equipos individuales. */
    private final int totalEquipment;

    /** Número de préstamos que todavía no han sido devueltos. */
    private final int activeLoans;

    /** Órdenes abiertas o en proceso. */
    private final int pendingOrders;

    /** Usuarios habilitados para utilizar el sistema. */
    private final int activeUsers;

    /** Construye el resumen inmutable. */
    public DashboardStats(
            int totalEquipment,
            int activeLoans,
            int pendingOrders,
            int activeUsers
    ) {
        this.totalEquipment = totalEquipment;
        this.activeLoans = activeLoans;
        this.pendingOrders = pendingOrders;
        this.activeUsers = activeUsers;
    }

    /** @return total de equipos. */
    public int getTotalEquipment() { return totalEquipment; }

    /** @return préstamos activos. */
    public int getActiveLoans() { return activeLoans; }

    /** @return órdenes pendientes. */
    public int getPendingOrders() { return pendingOrders; }

    /** @return usuarios activos. */
    public int getActiveUsers() { return activeUsers; }
}

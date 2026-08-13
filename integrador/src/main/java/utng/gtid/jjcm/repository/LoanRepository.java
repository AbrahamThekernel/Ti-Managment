package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.LoanRequest;
import utng.gtid.jjcm.model.LoanView;

/**
 * Persistencia transaccional de préstamos y devoluciones.
 */
public final class LoanRepository {

    /** Formato legible utilizado como primera parte de cada folio. */
    private static final DateTimeFormatter FOLIO_DATE =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

    /** Bloquea el producto hasta terminar la transacción para proteger el stock. */
    private static final String LOCK_PRODUCT_SQL =
            "SELECT stock FROM productos WHERE id = ? AND activo = TRUE FOR UPDATE";

    /** Inserta el encabezado y devuelve la llave generada por PostgreSQL. */
    private static final String INSERT_LOAN_SQL =
            "INSERT INTO prestamos "
            + "(folio, usuario_id, registrado_por, fecha_prestamo, "
            + "fecha_devolucion_programada, estado, motivo, observaciones, "
            + "responsiva_aceptada) "
            + "VALUES (?, ?, ?, ?, ?, 'ACTIVO', ?, ?, ?) RETURNING id";

    /** Inserta el producto y cantidad pertenecientes al préstamo. */
    private static final String INSERT_DETAIL_SQL =
            "INSERT INTO detalle_prestamo (prestamo_id, producto_id, cantidad) "
            + "VALUES (?, ?, ?)";

    /** Descuenta unidades después de bloquear y comprobar el stock. */
    private static final String DECREASE_STOCK_SQL =
            "UPDATE productos SET stock = stock - ?, actualizado_en = CURRENT_TIMESTAMP "
            + "WHERE id = ?";

    /** Convierte automáticamente en vencidos los préstamos cuya fecha terminó. */
    private static final String MARK_OVERDUE_SQL =
            "UPDATE prestamos SET estado = 'VENCIDO' "
            + "WHERE estado = 'ACTIVO' AND fecha_devolucion_programada < CURRENT_DATE";

    /** Consulta del listado que se muestra en la pantalla de Préstamos. */
    private static final String FIND_ALL_SQL =
            "SELECT p.id AS prestamo_id, d.id AS detalle_id, p.folio, "
            + "CONCAT(u.nombre, ' ', u.apellidos) AS solicitante, "
            + "pr.nombre AS producto, d.cantidad, "
            + "d.cantidad - d.cantidad_devuelta AS cantidad_pendiente, "
            + "p.fecha_prestamo, p.fecha_devolucion_programada, p.estado "
            + "FROM prestamos p "
            + "JOIN usuarios u ON u.id = p.usuario_id "
            + "JOIN detalle_prestamo d ON d.prestamo_id = p.id "
            + "JOIN productos pr ON pr.id = d.producto_id "
            + "ORDER BY p.creado_en DESC, d.id DESC";

    /** Bloquea el detalle que se devolverá y obtiene su producto relacionado. */
    private static final String LOCK_DETAIL_SQL =
            "SELECT prestamo_id, producto_id, cantidad - cantidad_devuelta AS pendiente "
            + "FROM detalle_prestamo WHERE id = ? FOR UPDATE";

    /** Guarda cada devolución para conservar trazabilidad completa. */
    private static final String INSERT_RETURN_SQL =
            "INSERT INTO devoluciones "
            + "(detalle_prestamo_id, recibido_por, cantidad, observaciones) "
            + "VALUES (?, ?, ?, ?)";

    /** Marca como devueltas todas las unidades pendientes del detalle. */
    private static final String COMPLETE_DETAIL_SQL =
            "UPDATE detalle_prestamo SET cantidad_devuelta = cantidad WHERE id = ?";

    /** Regresa al inventario la cantidad entregada por el usuario. */
    private static final String INCREASE_STOCK_SQL =
            "UPDATE productos SET stock = stock + ?, actualizado_en = CURRENT_TIMESTAMP "
            + "WHERE id = ?";

    /** Comprueba si todavía existen productos pendientes en el mismo préstamo. */
    private static final String PENDING_DETAILS_SQL =
            "SELECT COUNT(*) FROM detalle_prestamo "
            + "WHERE prestamo_id = ? AND cantidad_devuelta < cantidad";

    /** Finaliza el encabezado cuando todos sus detalles fueron devueltos. */
    private static final String COMPLETE_LOAN_SQL =
            "UPDATE prestamos SET estado = 'DEVUELTO', fecha_devolucion_real = CURRENT_DATE "
            + "WHERE id = ?";

    /** Cuenta préstamos activos o vencidos. */
    private static final String COUNT_ACTIVE_SQL =
            "SELECT COUNT(*) FROM prestamos WHERE estado IN ('ACTIVO', 'VENCIDO')";

    /** Cuenta préstamos completamente devueltos. */
    private static final String COUNT_RETURNED_SQL =
            "SELECT COUNT(*) FROM prestamos WHERE estado = 'DEVUELTO'";

    /**
     * Registra encabezado, detalle y descuento de stock en una sola transacción.
     *
     * @return folio generado para mostrarlo al usuario.
     */
    public String create(LoanRequest request) {
        String folio = generateFolio();

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int available = lockAndReadStock(connection, request.getProductId());
                if (available < request.getQuantity()) {
                    throw new DatabaseException(
                            "Stock insuficiente. Disponibles: " + available + "."
                    );
                }

                long loanId = insertLoan(connection, folio, request);
                insertDetail(connection, loanId, request);
                decreaseStock(connection, request.getProductId(), request.getQuantity());
                connection.commit();
                return folio;
            } catch (SQLException | DatabaseException error) {
                rollbackQuietly(connection);
                if (error instanceof DatabaseException) {
                    throw (DatabaseException) error;
                }
                throw new DatabaseException("No se pudo registrar el préstamo.", error);
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo abrir la transacción del préstamo.", error);
        }
    }

    /**
     * Devuelve el historial y actualiza previamente los estados vencidos.
     */
    public List<LoanView> findAll() {
        List<LoanView> loans = new ArrayList<>();
        try (Connection connection = Database.getConnection()) {
            try (PreparedStatement overdue = connection.prepareStatement(MARK_OVERDUE_SQL)) {
                overdue.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    loans.add(mapLoan(result));
                }
            }
            return loans;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar los préstamos.", error);
        }
    }

    /**
     * Registra la devolución completa del detalle indicado.
     */
    public void returnAll(long detailId, long receivedById, String notes) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ReturnData data = lockReturnData(connection, detailId);
                if (data.pending <= 0) {
                    throw new DatabaseException("Este producto ya fue devuelto.");
                }

                insertReturn(connection, detailId, receivedById, data.pending, notes);
                completeDetail(connection, detailId);
                increaseStock(connection, data.productId, data.pending);

                if (countPendingDetails(connection, data.loanId) == 0) {
                    completeLoan(connection, data.loanId);
                }
                connection.commit();
            } catch (SQLException | DatabaseException error) {
                rollbackQuietly(connection);
                if (error instanceof DatabaseException) {
                    throw (DatabaseException) error;
                }
                throw new DatabaseException("No se pudo registrar la devolución.", error);
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo abrir la transacción de devolución.", error);
        }
    }

    /** @return número de préstamos todavía pendientes. */
    public int countActive() {
        return executeCount(COUNT_ACTIVE_SQL, "No se pudieron contar los préstamos activos.");
    }

    /** @return número de préstamos devueltos. */
    public int countReturned() {
        return executeCount(COUNT_RETURNED_SQL, "No se pudieron contar las devoluciones.");
    }

    /** Bloquea el producto y devuelve su stock actual. */
    private int lockAndReadStock(Connection connection, long productId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_PRODUCT_SQL)) {
            statement.setLong(1, productId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new DatabaseException("El producto no existe o está inactivo.");
                }
                return result.getInt("stock");
            }
        }
    }

    /** Inserta el encabezado y devuelve su id. */
    private long insertLoan(Connection connection, String folio, LoanRequest request)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_LOAN_SQL)) {
            statement.setString(1, folio);
            statement.setLong(2, request.getBorrowerId());
            statement.setLong(3, request.getRegisteredById());
            statement.setDate(4, Date.valueOf(request.getLoanDate()));
            statement.setDate(5, Date.valueOf(request.getDueDate()));
            statement.setString(6, emptyToNull(request.getReason()));
            statement.setString(7, emptyToNull(request.getNotes()));
            statement.setBoolean(8, request.isResponsibilityAccepted());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong("id");
            }
        }
    }

    /** Inserta el único producto capturado actualmente por el formulario. */
    private void insertDetail(Connection connection, long loanId, LoanRequest request)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_DETAIL_SQL)) {
            statement.setLong(1, loanId);
            statement.setLong(2, request.getProductId());
            statement.setInt(3, request.getQuantity());
            statement.executeUpdate();
        }
    }

    /** Descuenta existencias dentro de la misma transacción. */
    private void decreaseStock(Connection connection, long productId, int quantity)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DECREASE_STOCK_SQL)) {
            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.executeUpdate();
        }
    }

    /** Obtiene y bloquea los datos requeridos para una devolución. */
    private ReturnData lockReturnData(Connection connection, long detailId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_DETAIL_SQL)) {
            statement.setLong(1, detailId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new DatabaseException("No existe el detalle de préstamo seleccionado.");
                }
                return new ReturnData(
                        result.getLong("prestamo_id"),
                        result.getLong("producto_id"),
                        result.getInt("pendiente")
                );
            }
        }
    }

    /** Inserta la evidencia histórica de devolución. */
    private void insertReturn(
            Connection connection,
            long detailId,
            long receivedById,
            int quantity,
            String notes
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_RETURN_SQL)) {
            statement.setLong(1, detailId);
            statement.setLong(2, receivedById);
            statement.setInt(3, quantity);
            statement.setString(4, emptyToNull(notes));
            statement.executeUpdate();
        }
    }

    /** Marca todas las unidades del detalle como devueltas. */
    private void completeDetail(Connection connection, long detailId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COMPLETE_DETAIL_SQL)) {
            statement.setLong(1, detailId);
            statement.executeUpdate();
        }
    }

    /** Devuelve las existencias al producto. */
    private void increaseStock(Connection connection, long productId, int quantity)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INCREASE_STOCK_SQL)) {
            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.executeUpdate();
        }
    }

    /** Cuenta detalles todavía pendientes dentro del mismo préstamo. */
    private int countPendingDetails(Connection connection, long loanId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(PENDING_DETAILS_SQL)) {
            statement.setLong(1, loanId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    /** Finaliza el encabezado y registra la fecha real. */
    private void completeLoan(Connection connection, long loanId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COMPLETE_LOAN_SQL)) {
            statement.setLong(1, loanId);
            statement.executeUpdate();
        }
    }

    /** Ejecuta una consulta COUNT común. */
    private int executeCount(String sql, String errorMessage) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException error) {
            throw new DatabaseException(errorMessage, error);
        }
    }

    /** Convierte una fila SQL en LoanView. */
    private LoanView mapLoan(ResultSet result) throws SQLException {
        return new LoanView(
                result.getLong("prestamo_id"),
                result.getLong("detalle_id"),
                result.getString("folio"),
                result.getString("solicitante"),
                result.getString("producto"),
                result.getInt("cantidad"),
                result.getInt("cantidad_pendiente"),
                result.getDate("fecha_prestamo").toLocalDate(),
                result.getDate("fecha_devolucion_programada").toLocalDate(),
                result.getString("estado")
        );
    }

    /** Genera un folio legible con un sufijo aleatorio para evitar duplicados. */
    private String generateFolio() {
        String date = LocalDateTime.now().format(FOLIO_DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        return "PRE-" + date + "-" + suffix;
    }

    /** Convierte textos vacíos en NULL. */
    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** Revierte una transacción sin reemplazar la excepción principal. */
    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // El error original contiene la información realmente importante.
        }
    }

    /** Datos internos obtenidos mientras el detalle está bloqueado. */
    private static final class ReturnData {
        /** Préstamo propietario del detalle. */
        private final long loanId;

        /** Producto que recibirá nuevamente las existencias. */
        private final long productId;

        /** Unidades que todavía faltan por devolver. */
        private final int pending;

        /** Construye el resultado interno de bloqueo. */
        private ReturnData(long loanId, long productId, int pending) {
            this.loanId = loanId;
            this.productId = productId;
            this.pending = pending;
        }
    }
}

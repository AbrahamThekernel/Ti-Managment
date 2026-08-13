package utng.gtid.jjcm.service;

import java.time.LocalDate;
import java.util.List;

import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;
import utng.gtid.jjcm.model.LoanRequest;
import utng.gtid.jjcm.model.LoanView;
import utng.gtid.jjcm.repository.CatalogRepository;
import utng.gtid.jjcm.repository.LoanRepository;

/**
 * Reglas de negocio de préstamos entre los controladores y PostgreSQL.
 */
public final class LoanService {

    /** Repositorio de listas desplegables. */
    private final CatalogRepository catalogRepository = new CatalogRepository();

    /** Repositorio de operaciones transaccionales. */
    private final LoanRepository loanRepository = new LoanRepository();

    /** @return usuarios activos mostrados en el formulario. */
    public List<CatalogItem> findBorrowers() {
        return catalogRepository.findActiveBorrowers();
    }

    /** @return productos con existencias disponibles. */
    public List<CatalogItem> findAvailableProducts() {
        return catalogRepository.findAvailableProducts();
    }

    /** @return préstamos ordenados del más reciente al más antiguo. */
    public List<LoanView> findAll() {
        return loanRepository.findAll();
    }

    /** @return número de préstamos pendientes. */
    public int countActive() {
        return loanRepository.countActive();
    }

    /** @return número de préstamos devueltos. */
    public int countReturned() {
        return loanRepository.countReturned();
    }

    /**
     * Valida y registra un préstamo real.
     *
     * @return folio asignado por el repositorio.
     */
    public String register(
            CatalogItem borrower,
            CatalogItem product,
            int quantity,
            LocalDate loanDate,
            LocalDate dueDate,
            String reason,
            String notes,
            boolean responsibilityAccepted
    ) {
        if (borrower == null) {
            throw new DatabaseException("Selecciona al profesor que solicita el préstamo.");
        }
        if (product == null) {
            throw new DatabaseException("Selecciona el equipo que se prestará.");
        }
        if (quantity <= 0) {
            throw new DatabaseException("La cantidad debe ser mayor que cero.");
        }
        if (quantity > product.getAvailable()) {
            throw new DatabaseException(
                    "Solo existen " + product.getAvailable() + " unidades disponibles."
            );
        }
        if (loanDate == null || dueDate == null) {
            throw new DatabaseException("Selecciona ambas fechas del préstamo.");
        }
        if (dueDate.isBefore(loanDate)) {
            throw new DatabaseException("La devolución no puede ser anterior al préstamo.");
        }
        if (!responsibilityAccepted) {
            throw new DatabaseException("Debes confirmar la responsiva del préstamo.");
        }

        long administratorId = catalogRepository.findDefaultAdministratorId();
        LoanRequest request = new LoanRequest(
                borrower.getId(),
                product.getId(),
                administratorId,
                quantity,
                loanDate,
                dueDate,
                reason,
                notes,
                true
        );
        return loanRepository.create(request);
    }

    /**
     * Registra la devolución total y devuelve las unidades al inventario.
     */
    public void returnLoan(long detailId) {
        long administratorId = catalogRepository.findDefaultAdministratorId();
        loanRepository.returnAll(detailId, administratorId, "Devolución registrada desde JavaFX");
    }
}

package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.CatalogItem;

/**
 * Consultas de solo lectura para llenar listas desplegables y catálogos.
 */
public final class CatalogRepository {

    /** Consulta de personas habilitadas para solicitar un préstamo. */
    private static final String BORROWERS_SQL =
            "SELECT u.id, CONCAT(u.nombre, ' ', u.apellidos) AS nombre_completo, "
            + "COALESCE(u.departamento, '') AS detalle "
            + "FROM usuarios u "
            + "WHERE u.activo = TRUE "
            + "ORDER BY u.nombre, u.apellidos";

    /** Consulta de productos que todavía tienen unidades disponibles. */
    private static final String PRODUCTS_SQL =
            "SELECT p.id, p.nombre, COALESCE(p.modelo, '') AS detalle, p.stock "
            + "FROM productos p "
            + "WHERE p.activo = TRUE AND p.stock > 0 "
            + "ORDER BY p.nombre";

    /** Consulta de categorías disponibles para altas de inventario. */
    private static final String CATEGORIES_SQL =
            "SELECT c.id, c.nombre, COALESCE(c.descripcion, '') AS detalle "
            + "FROM categorias c WHERE c.activo = TRUE ORDER BY c.nombre";

    /** Consulta de ubicaciones disponibles para asignar equipos. */
    private static final String LOCATIONS_SQL =
            "SELECT u.id, u.nombre, COALESCE(u.edificio, '') AS detalle "
            + "FROM ubicaciones u WHERE u.activo = TRUE ORDER BY u.nombre";

    /** Consulta compacta de equipos utilizada en formularios relacionados. */
    private static final String EQUIPMENT_SQL =
            "SELECT e.id, e.codigo || ' - ' || e.nombre AS nombre, "
            + "COALESCE(e.modelo, '') AS detalle "
            + "FROM equipos e WHERE e.estado <> 'BAJA' ORDER BY e.codigo";

    /** Consulta de técnicos que pueden atender mantenimientos. */
    private static final String TECHNICIANS_SQL =
            "SELECT u.id, CONCAT(u.nombre, ' ', u.apellidos) AS nombre, "
            + "COALESCE(u.departamento, '') AS detalle "
            + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
            + "WHERE u.activo = TRUE AND r.nombre IN ('TECNICO', 'ADMINISTRADOR') "
            + "ORDER BY u.nombre, u.apellidos";

    /**
     * Consulta del responsable provisional de las operaciones.
     * Prefiere un administrador, pero permite continuar con cualquier usuario
     * activo cuando todos los administradores fueron desactivados.
     */
    private static final String ADMINISTRATOR_SQL =
            "SELECT u.id "
            + "FROM usuarios u JOIN roles r ON r.id = u.rol_id "
            + "WHERE u.activo = TRUE "
            + "ORDER BY CASE WHEN r.nombre = 'ADMINISTRADOR' THEN 0 ELSE 1 END, u.id "
            + "LIMIT 1";

    /**
     * Obtiene los usuarios activos que pueden aparecer en un préstamo.
     */
    public List<CatalogItem> findActiveBorrowers() {
        List<CatalogItem> borrowers = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(BORROWERS_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                borrowers.add(new CatalogItem(
                        result.getLong("id"),
                        result.getString("nombre_completo"),
                        result.getString("detalle"),
                        0
                ));
            }
            return borrowers;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar los usuarios activos.", error);
        }
    }

    /**
     * Obtiene productos activos con stock mayor que cero.
     */
    public List<CatalogItem> findAvailableProducts() {
        List<CatalogItem> products = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(PRODUCTS_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                products.add(new CatalogItem(
                        result.getLong("id"),
                        result.getString("nombre"),
                        result.getString("detalle"),
                        result.getInt("stock")
                ));
            }
            return products;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar los productos disponibles.", error);
        }
    }

    /**
     * Obtiene categorías activas para crear o editar productos.
     */
    public List<CatalogItem> findActiveCategories() {
        List<CatalogItem> categories = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(CATEGORIES_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                categories.add(new CatalogItem(
                        result.getLong("id"),
                        result.getString("nombre"),
                        result.getString("detalle"),
                        0
                ));
            }
            return categories;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar las categorías.", error);
        }
    }

    /** Obtiene ubicaciones activas para el formulario de equipos. */
    public List<CatalogItem> findActiveLocations() {
        return findCatalog(LOCATIONS_SQL, "No se pudieron consultar las ubicaciones.");
    }

    /** Obtiene equipos que todavía pueden relacionarse con nuevas operaciones. */
    public List<CatalogItem> findUsableEquipment() {
        return findCatalog(EQUIPMENT_SQL, "No se pudieron consultar los equipos.");
    }

    /** Obtiene técnicos y administradores habilitados. */
    public List<CatalogItem> findActiveTechnicians() {
        return findCatalog(TECHNICIANS_SQL, "No se pudieron consultar los técnicos.");
    }

    /** Ejecuta consultas de catálogo que comparten id, nombre y detalle. */
    private List<CatalogItem> findCatalog(String sql, String errorMessage) {
        List<CatalogItem> items = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                items.add(new CatalogItem(
                        result.getLong("id"),
                        result.getString("nombre"),
                        result.getString("detalle"),
                        0
                ));
            }
            return items;
        } catch (SQLException error) {
            throw new DatabaseException(errorMessage, error);
        }
    }

    /**
     * Devuelve el primer administrador activo o, como respaldo, cualquier
     * usuario activo mientras no existe un contexto global de sesión.
     */
    public long findDefaultAdministratorId() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(ADMINISTRATOR_SQL);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return result.getLong("id");
            }
            throw new DatabaseException(
                    "No existe ningún usuario activo para registrar la operación."
            );
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo localizar al administrador inicial.", error);
        }
    }
}

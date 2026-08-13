package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.ProductView;

/**
 * Operaciones CRUD del inventario de productos prestables.
 */
public final class ProductRepository {

    /** Consulta base utilizada para pintar el listado de inventario. */
    private static final String FIND_ALL_SQL =
            "SELECT p.id, p.categoria_id, c.nombre AS categoria, p.nombre, "
            + "COALESCE(p.modelo, '') AS modelo, p.stock, p.stock_minimo, p.activo "
            + "FROM productos p JOIN categorias c ON c.id = p.categoria_id "
            + "ORDER BY p.nombre";

    /** Inserción parametrizada que evita inyección SQL. */
    private static final String INSERT_SQL =
            "INSERT INTO productos "
            + "(categoria_id, nombre, modelo, stock, stock_minimo, activo) "
            + "VALUES (?, ?, ?, ?, ?, TRUE)";

    /** Actualización parametrizada de todos los campos editables. */
    private static final String UPDATE_SQL =
            "UPDATE productos SET categoria_id = ?, nombre = ?, modelo = ?, "
            + "stock = ?, stock_minimo = ?, actualizado_en = CURRENT_TIMESTAMP "
            + "WHERE id = ?";

    /** Baja lógica que conserva el historial relacionado. */
    private static final String TOGGLE_SQL =
            "UPDATE productos SET activo = NOT activo, actualizado_en = CURRENT_TIMESTAMP "
            + "WHERE id = ?";

    /** Devuelve todos los productos y sus categorías. */
    public List<ProductView> findAll() {
        List<ProductView> products = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                products.add(mapProduct(result));
            }
            return products;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo consultar el inventario.", error);
        }
    }

    /** Crea un producto nuevo en PostgreSQL. */
    public void insert(long categoryId, String name, String model, int stock, int minimumStock) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, categoryId);
            statement.setString(2, name);
            statement.setString(3, emptyToNull(model));
            statement.setInt(4, stock);
            statement.setInt(5, minimumStock);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo crear el producto.", error);
        }
    }

    /** Actualiza un producto existente. */
    public void update(
            long id,
            long categoryId,
            String name,
            String model,
            int stock,
            int minimumStock
    ) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setLong(1, categoryId);
            statement.setString(2, name);
            statement.setString(3, emptyToNull(model));
            statement.setInt(4, stock);
            statement.setInt(5, minimumStock);
            statement.setLong(6, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo actualizar el producto.", error);
        }
    }

    /** Activa o desactiva un producto sin eliminar sus relaciones históricas. */
    public void toggleActive(long id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(TOGGLE_SQL)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo cambiar el estado del producto.", error);
        }
    }

    /** Convierte la fila actual de ResultSet en un objeto de dominio. */
    private ProductView mapProduct(ResultSet result) throws SQLException {
        return new ProductView(
                result.getLong("id"),
                result.getLong("categoria_id"),
                result.getString("categoria"),
                result.getString("nombre"),
                result.getString("modelo"),
                result.getInt("stock"),
                result.getInt("stock_minimo"),
                result.getBoolean("activo")
        );
    }

    /** PostgreSQL recibe NULL cuando un texto opcional está vacío. */
    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

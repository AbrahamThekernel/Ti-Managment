package utng.gtid.jjcm.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import utng.gtid.jjcm.database.Database;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.EquipmentView;

/**
 * Repositorio JDBC que implementa el CRUD de equipos individuales.
 */
public final class EquipmentRepository {

    /** Consulta con nombres de categoría y ubicación ya resueltos. */
    private static final String FIND_ALL_SQL =
            "SELECT e.id, e.categoria_id, e.ubicacion_id, e.codigo, e.nombre, "
            + "c.nombre AS categoria, COALESCE(e.modelo, '') AS modelo, "
            + "COALESCE(e.numero_serie, '') AS numero_serie, e.estado, "
            + "COALESCE(u.nombre, 'Sin asignar') AS ubicacion, "
            + "COALESCE(e.observaciones, '') AS observaciones "
            + "FROM equipos e JOIN categorias c ON c.id = e.categoria_id "
            + "LEFT JOIN ubicaciones u ON u.id = e.ubicacion_id "
            + "ORDER BY e.codigo";

    /** Inserción parametrizada de un nuevo equipo. */
    private static final String INSERT_SQL =
            "INSERT INTO equipos "
            + "(categoria_id, ubicacion_id, codigo, nombre, modelo, numero_serie, estado, observaciones) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /** Actualización parametrizada de los campos editables. */
    private static final String UPDATE_SQL =
            "UPDATE equipos SET categoria_id = ?, ubicacion_id = ?, codigo = ?, "
            + "nombre = ?, modelo = ?, numero_serie = ?, estado = ?, observaciones = ?, "
            + "actualizado_en = CURRENT_TIMESTAMP WHERE id = ?";

    /** Devuelve todos los equipos con sus catálogos. */
    public List<EquipmentView> findAll() {
        List<EquipmentView> equipment = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                equipment.add(mapEquipment(result));
            }
            return equipment;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar los equipos.", error);
        }
    }

    /** Crea un equipo nuevo. */
    public void insert(
            long categoryId,
            Long locationId,
            String code,
            String name,
            String model,
            String serialNumber,
            String status,
            String observations
    ) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            setCommonFields(statement, categoryId, locationId, code, name,
                    model, serialNumber, status, observations);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException(
                    "No se pudo crear el equipo. Verifica que código y serie no estén repetidos.",
                    error
            );
        }
    }

    /** Actualiza un equipo existente. */
    public void update(
            long id,
            long categoryId,
            Long locationId,
            String code,
            String name,
            String model,
            String serialNumber,
            String status,
            String observations
    ) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            setCommonFields(statement, categoryId, locationId, code, name,
                    model, serialNumber, status, observations);
            statement.setLong(9, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new DatabaseException(
                    "No se pudo actualizar el equipo. Verifica código y serie.",
                    error
            );
        }
    }

    /** Coloca los ocho parámetros compartidos por INSERT y UPDATE. */
    private void setCommonFields(
            PreparedStatement statement,
            long categoryId,
            Long locationId,
            String code,
            String name,
            String model,
            String serialNumber,
            String status,
            String observations
    ) throws SQLException {
        statement.setLong(1, categoryId);
        if (locationId == null) {
            statement.setNull(2, Types.BIGINT);
        } else {
            statement.setLong(2, locationId);
        }
        statement.setString(3, code);
        statement.setString(4, name);
        statement.setString(5, emptyToNull(model));
        statement.setString(6, emptyToNull(serialNumber));
        statement.setString(7, status);
        statement.setString(8, emptyToNull(observations));
    }

    /** Convierte la fila SQL actual a EquipmentView. */
    private EquipmentView mapEquipment(ResultSet result) throws SQLException {
        long rawLocationId = result.getLong("ubicacion_id");
        Long locationId = result.wasNull() ? null : rawLocationId;
        return new EquipmentView(
                result.getLong("id"),
                result.getLong("categoria_id"),
                locationId,
                result.getString("codigo"),
                result.getString("nombre"),
                result.getString("categoria"),
                result.getString("modelo"),
                result.getString("numero_serie"),
                result.getString("estado"),
                result.getString("ubicacion"),
                result.getString("observaciones")
        );
    }

    /** Envía NULL para textos opcionales vacíos. */
    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

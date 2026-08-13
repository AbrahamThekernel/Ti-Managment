package utng.gtid2.inicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrestamosDAO {

    public int prestamosDAO() {
        
    String sql = """
        SELECT COUNT(*)
        FROM prestamos
        WHERE estado = 'ACTIVO'
    """;

    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            return rs.getInt(1);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return 0;
}
}

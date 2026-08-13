package utng.gtid2.inicio.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ActualizarLabelDAO {

    private void cargarTotalEquipos() {

    String sql = "SELECT COUNT(*) FROM equipos";

    try (Connection conn = ConexionBD.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        if (rs.next()) {
            int total = rs.getInt(1);
            lblTotalEquipos.setText(String.valueOf(total));
        }

    } catch (SQLException e) {
        e.printStackTrace();
        lblTotalEquipos.setText("Error");
    }
}    
@Override
public void initialize(URL url, ResourceBundle rb) {
    cargarTotalEquipos();
}
}


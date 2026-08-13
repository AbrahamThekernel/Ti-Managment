module utng.gtid.jjcm {
    // Permite utilizar controles, escenas y ventanas JavaFX.
    requires javafx.controls;

    // Permite cargar los diseños declarados en archivos FXML.
    requires javafx.fxml;

    // Permite utilizar Connection, PreparedStatement y las demás clases JDBC.
    requires java.sql;

    // Incluye el controlador que implementa el protocolo JDBC de PostgreSQL.
    requires org.postgresql.jdbc;

    // Permite que FXMLLoader acceda a campos y métodos marcados con @FXML.
    opens utng.gtid.jjcm to javafx.fxml;

    // Expone el paquete principal para que JavaFX pueda iniciar la aplicación.
    exports utng.gtid.jjcm;
}

module utng.gtid2.inicio {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens utng.gtid2.inicio to javafx.fxml;
    opens utng.gtid2.inicio.Controller to javafx.fxml;
    exports utng.gtid2.inicio;
    exports utng.gtid2.inicio.Controller;
}


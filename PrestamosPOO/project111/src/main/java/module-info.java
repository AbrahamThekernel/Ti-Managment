module utng.gtid2.jjsh {
    requires javafx.controls;
    requires javafx.fxml;

    opens utng.gtid2.jjsh to javafx.fxml;
    
    exports utng.gtid2.jjsh;
}
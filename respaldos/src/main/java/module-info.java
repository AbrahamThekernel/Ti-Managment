module respaldos {
    requires javafx.controls;
    requires javafx.fxml;

    opens respaldos to javafx.fxml;
    exports respaldos;
}

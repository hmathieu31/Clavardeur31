module com.insa.projet4a {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.insa.projet4a to javafx.fxml;
    exports com.insa.projet4a;
}

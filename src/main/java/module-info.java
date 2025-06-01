module com.example.spiceyumm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.opencsv;
    requires json.simple;
    requires java.net.http;
    requires javafx.graphics;


    opens com.example.spiceyumm to javafx.fxml;
    exports com.example.spiceyumm;
}
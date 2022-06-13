module com.engine.chess {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.engine.chess to javafx.fxml;
    exports com.engine.chess;
}
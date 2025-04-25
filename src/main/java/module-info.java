module edu.sharif.blueprinthell {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens edu.sharif.blueprinthell to javafx.fxml;
    exports edu.sharif.blueprinthell;
}
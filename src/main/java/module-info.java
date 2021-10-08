module com.shank.offcoder {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.shank.offcoder to javafx.fxml;
    exports com.shank.offcoder;
}
module com.shank.offcoder {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    requires org.apache.commons.codec;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    requires com.sun.jna;
    requires org.json;

    opens com.shank.offcoder to javafx.fxml;
    exports com.shank.offcoder;
    opens com.shank.offcoder.app to javafx.fxml;
    exports com.shank.offcoder.app;
}
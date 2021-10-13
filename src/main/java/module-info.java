module com.shank.offcoder {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    requires org.apache.commons.codec;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    requires com.sun.jna;
    requires org.json;
    requires org.jsoup;

    opens com.shank.offcoder to javafx.fxml;
    exports com.shank.offcoder;
    opens com.shank.offcoder.app to javafx.fxml;
    exports com.shank.offcoder.app;
    opens com.shank.offcoder.cf to javafx.fxml;
    exports com.shank.offcoder.cf;
    opens com.shank.offcoder.controllers to javafx.fxml;
    exports com.shank.offcoder.controllers;
}
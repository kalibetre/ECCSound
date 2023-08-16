module com.eccsound {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires org.bouncycastle.provider;

    opens com.eccsound to javafx.fxml;
    exports com.eccsound;
    exports com.eccsound.controller;
}
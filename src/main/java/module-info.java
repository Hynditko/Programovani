module cz.spse.prj08_javafx_fxml_login {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens cz.spse.prj08_javafx_fxml_login to javafx.fxml;
    exports cz.spse.prj08_javafx_fxml_login;
}
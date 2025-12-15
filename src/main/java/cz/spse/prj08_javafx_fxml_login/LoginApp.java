package cz.spse.prj08_javafx_fxml_login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoginApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApp.class.getResource("Login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 320);

        scene.getStylesheets()
                        .add(LoginApp.class.getResource("login.css").toExternalForm());

        stage.setTitle("Login JavaFx");
        stage.setScene(scene);

        stage.initStyle(StageStyle.UNDECORATED);

        stage.show();
    }
}

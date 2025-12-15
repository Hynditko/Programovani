package cz.spse.prj08_javafx_fxml_login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button btnLogin;

    @FXML
    private PasswordField heslo;

    @FXML
    private TextField jmeno;

    @FXML
    private Label output;

    @FXML
    private Label login;

    @FXML
    void myLoginHandler(ActionEvent event) throws IOException {

//        String pom = heslo.getText();
//        output.setText(pom);

        String jm = jmeno.getText();
        String psw = heslo.getText();

        if (jm.equals("admin") && psw.equals("123")) {
            //output.setText("Welcome to Agartha");
            //... volani nasi nove aplikace - metodou
            mojeAplikace(event);
        } else {
            output.setText("You are not welcomed in Agartha");
        }

    }

    private void mojeAplikace(ActionEvent event) throws IOException {

        ( (Node) (event.getSource())).getScene().getWindow().hide();

        // **Důležitá změna: Kontrola existence FXML souboru**
        URL fxmlUrl = getClass().getResource("contact-manager-view.fxml");
        if (fxmlUrl == null) {
            // Můžete změnit na "throw new Exception(...)" pokud to IDE vyžaduje, ale IOException je standardní.
            throw new IOException("Chyba: FXML soubor 'contact-manager-view.fxml' nebyl nalezen. Zkontrolujte cestu v resources!");
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scena = new Scene(root);

        // **Důležitá změna: Kontrola existence CSS souboru**
        URL cssUrl = getClass().getResource("contact.css");
        if (cssUrl != null) {
            scena.getStylesheets()
                    .add(cssUrl.toExternalForm());
        } else {
            // Vypíše varování, pokud se CSS nenajde, ale aplikace nespadne
            System.err.println("Varování: CSS soubor 'contact.css' nebyl nalezen. Aplikace poběží bez stylů.");
        }
        Stage stage = new Stage();
        stage.setScene(scena);
        stage.setTitle("Správce Kontaktů");
        stage.show();

    }

    // ... design - init
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        login.setId("login-text");
        output.setId("output-text");

    }
}

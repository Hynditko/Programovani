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
import java.util.regex.Pattern;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button btnLogin;

    @FXML
    private PasswordField heslo;

    // --- ZMĚNA 1: Přejmenování proměnné, aby odpovídala novému ID v FXML ---
    @FXML
    private TextField emailField;

    @FXML
    private Label output;

    @FXML
    private Label login;

    // --- ZMĚNA 2: Definice Regexu pro Email ---
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @FXML
    void myLoginHandler(ActionEvent event) throws IOException {
        // --- ZMĚNA 3: Načítáme email místo jména ---
        String email = emailField.getText();
        String psw = heslo.getText();

        // Validace emailu při loginu (dle zadání)
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            output.setText("Neplatný formát emailu!");
            return;
        }

        // Kontrola přihlášení proti souboru uzivatele.txt
        if (checkCredentials(email, psw)) {
            mojeAplikace(event);
        } else {
            output.setText("Chyba přihlášení!");
        }
    }

    @FXML
    void myRegisterHandler(ActionEvent event) {
        // --- ZMĚNA 4: Načítáme email ---
        String email = emailField.getText();
        String psw = heslo.getText();

        if (email.isEmpty() || psw.isEmpty()) {
            output.setText("Vyplňte email a heslo!");
            return;
        }

        // --- ZMĚNA 5: Kontrola Regexu pro Email ---
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            output.setText("Zadejte platný email (např. jan@post.cz)!");
            return;
        }

        // Původní kontrola hesla (ponechána)
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,20}$";
        if (!psw.matches(passwordRegex)) {
            output.setText("Heslo nesplňuje požadavky (8-20 znaků, A, a, 1)!");
            return;
        }

        File souborUzivatele = getDatovySoubor("uzivatele.txt");

        // Kontrola, zda už email neexistuje (volitelné vylepšení)
        if (checkUserExists(email)) {
            output.setText("Tento email je již registrován!");
            return;
        }

        try (FileWriter writer = new FileWriter(souborUzivatele, true)) {
            // Zapisujeme ve formátu email:heslo
            writer.write(email + ":" + psw + System.lineSeparator());
            output.setText("Registrace úspěšná!");
        } catch (IOException e) {
            output.setText("Chyba při zápisu: " + e.getMessage());
        }
    }

    private boolean checkCredentials(String email, String password) {
        File file = getDatovySoubor("uzivatele.txt");

        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    // Porovnáváme email (parts[0]) a heslo (parts[1])
                    if (parts[0].equals(email) && parts[1].equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Pomocná metoda pro kontrolu duplicity při registraci
    private boolean checkUserExists(String email) {
        File file = getDatovySoubor("uzivatele.txt");
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 1 && parts[0].equals(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void mojeAplikace(ActionEvent event) throws IOException {
        ((Node) (event.getSource())).getScene().getWindow().hide();

        // --- ZMĚNA 6: Předáváme přihlášený email ---
        String prihlasenyUzivatel = emailField.getText();

        URL fxmlUrl = getClass().getResource("contact-manager-view.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Chyba: FXML soubor 'contact-manager-view.fxml' nebyl nalezen.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        ContactManagerController controller = loader.getController();
        controller.nastaveniUzivatele(prihlasenyUzivatel); // Zde se předá email, vytvoří se kontakty_email@test.cz.txt

        Scene scena = new Scene(root);

        URL cssUrl = getClass().getResource("contact.css");
        if (cssUrl != null) {
            scena.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = new Stage();
        stage.setScene(scena);
        stage.setTitle("Správce Kontaktů - " + prihlasenyUzivatel);
        stage.show();
    }

    private File getDatovySoubor(String nazevSouboru) {
        String userHome = System.getProperty("user.home");
        File dataDir = new File(userHome, ".contactmanager");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        return new File(dataDir, nazevSouboru);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        login.setId("login-text");
        output.setId("output-text");
    }
}

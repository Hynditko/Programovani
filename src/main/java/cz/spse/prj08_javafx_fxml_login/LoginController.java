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

import java.io.*;
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

        String jm = jmeno.getText();
        String psw = heslo.getText();

        // Kontrola přihlášení proti souboru uzivatele.txt
        if (checkCredentials(jm, psw)) {
            mojeAplikace(event);
        } else {
            output.setText("Chyba!");
        }

    }

    @FXML
    void myRegisterHandler(ActionEvent event) {
        String jm = jmeno.getText();
        String psw = heslo.getText();

        if (jm.isEmpty() || psw.isEmpty()) {
            output.setText("Vyplňte jméno a heslo!");
            return;
        }

        // Použijeme getDatovySoubor pro získání bezpečné cesty
        File souborUzivatele = getDatovySoubor("uzivatele.txt");

        try (FileWriter writer = new FileWriter(souborUzivatele, true)) {
            writer.write(jm + ":" + psw + System.lineSeparator());
            output.setText("Registrace úspěšná!");
        } catch (IOException e) {
            output.setText("Chyba při zápisu: " + e.getMessage());
        }
    }

    // Pomocná metoda pro kontrolu údajů v souboru
    private boolean checkCredentials(String username, String password) {
        // OPRAVA: Musíme použít getDatovySoubor, aby program věděl, že má hledat v .contactmanager
        File file = getDatovySoubor("uzivatele.txt");

        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // OPRAVA: split(":") místo split(";"), protože v registraci máte dvojtečku
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    if (parts[0].equals(username) && parts[1].equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void mojeAplikace(ActionEvent event) throws IOException {

        ( (Node) (event.getSource())).getScene().getWindow().hide();

        // 1. Získáme jméno aktuálně přihlášeného uživatele z textového pole
        String prihlasenyUzivatel = jmeno.getText();

        URL fxmlUrl = getClass().getResource("contact-manager-view.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Chyba: FXML soubor 'contact-manager-view.fxml' nebyl nalezen.");
        }

        // 2. Změna způsobu načítání - vytvoříme instanci loaderu
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load(); // Načteme View

        // 3. Získáme Controller z loaderu a předáme mu uživatele
        ContactManagerController controller = loader.getController();
        controller.nastaveniUzivatele(prihlasenyUzivatel);

        Scene scena = new Scene(root);

        // **Důležitá změna: Kontrola existence CSS souboru**
        URL cssUrl = getClass().getResource("contact.css");
        if (cssUrl != null) {
            scena.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            // Vypíše varování, pokud se CSS nenajde, ale aplikace nespadne
            System.err.println("Varování: CSS soubor 'contact.css' nebyl nalezen. Aplikace poběží bez stylů.");
        }
        Stage stage = new Stage();
        stage.setScene(scena);
        stage.setTitle("Správce Kontaktů");
        stage.show();

    }

    // Metoda pro získání cesty k datové složce uživatele
    private File getDatovySoubor(String nazevSouboru) {
        // Získá cestu k domovské složce uživatele (C:\Users\Jmeno)
        String userHome = System.getProperty("user.home");
        // Vytvoří cestu k naší složce .contactmanager
        File dataDir = new File(userHome, ".contactmanager");

        // Pokud složka neexistuje, vytvoříme ji
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        // Vrátí odkaz na konkrétní soubor v této složce
        return new File(dataDir, nazevSouboru);
    }

    // ... design - init
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        login.setId("login-text");
        output.setId("output-text");

    }
}

package cz.spse.prj08_javafx_fxml_login;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.util.regex.Pattern;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ContactManagerController {

    // Seznam pro uchování kontaktů
    private final ObservableList<Kontakt> seznamKontaktu = FXCollections.observableArrayList();

    // Regex pro email:
    // Příklad: 'uzivatel@domena.tld'
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    // Regex pro telefonní číslo (český formát):
    // Příklad: '+420 123 456 789' nebo '777111222'
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+\\d{3} ?)?(\\d{3} ?){2}\\d{3}$"
    );

    @FXML
    private TableView<Kontakt> tableViewKontakty;

    @FXML
    private TableColumn<Kontakt, String> colJmeno;

    @FXML
    private TableColumn<Kontakt, String> colPrijmeni;

    @FXML
    private TableColumn<Kontakt, String> colTelefon;

    @FXML
    private TableColumn<Kontakt, String> colEmail;

    @FXML
    private TextField txtJmeno;

    @FXML
    private TextField txtPrijmeni;

    @FXML
    private TextField txtTelefon;

    @FXML
    private TextField txtEmail;

    @FXML
    private Label lblStatus;

    @FXML
    private VBox contactDetailPane;

    @FXML
    private Label lblJmenoPrijmeni;

    @FXML
    private Label lblDetailTelefon;

    @FXML
    private Label lblDetailEmail;

    // Inicializace
    @FXML
    public void initialize() {
        // Nastavení, jaké atributy z třídy Kontakt se zobrazí ve sloupcích
        colJmeno.setCellValueFactory(cellData -> cellData.getValue().jmenoProperty());
        colPrijmeni.setCellValueFactory(cellData -> cellData.getValue().prijmeniProperty());

        // Odebrali jsme zobrazení sloupce Telefon a Email z tabulky.
        // Tyto sloupce v FXML souboru už nejsou.

        // Nastavení seznamu kontaktů pro TableView
        tableViewKontakty.setItems(seznamKontaktu);

        // ... (Přidání ukázkových dat)

        lblStatus.setText("Připraveno");

        // --- LOGIKA PRO ZOBRAZENÍ DETAILŮ ---

        // Skrýt detaily na začátku (nebo zobrazit výchozí zprávu)
        lblJmenoPrijmeni.setText("Vyberte kontakt ze seznamu");
        lblDetailTelefon.setText("");
        lblDetailEmail.setText("");

        // Přidání listeneru na výběr kontaktu v tabulce
        tableViewKontakty.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Kontakt>() {
                    @Override
                    public void changed(ObservableValue<? extends Kontakt> observable, Kontakt oldValue, Kontakt newValue) {
                        zobrazDetailKontaktu(newValue);
                    }
                });
    }

    // Nová metoda pro zobrazení detailů vybraného kontaktu
    private void zobrazDetailKontaktu(Kontakt kontakt) {
        if (kontakt != null) {
            lblJmenoPrijmeni.setText(kontakt.getJmeno() + " " + kontakt.getPrijmeni());
            lblDetailTelefon.setText("Telefon: " + kontakt.getTelefon());
            lblDetailEmail.setText("Email: " + kontakt.getEmail());
            // Zde by bylo možné přidat logiku pro zobrazení, pokud byl panel skryt
            // contactDetailPane.setVisible(true);
        } else {
            // Když není vybrán žádný kontakt
            lblJmenoPrijmeni.setText("Vyberte kontakt ze seznamu");
            lblDetailTelefon.setText("");
            lblDetailEmail.setText("");
        }
    }

    @FXML
    void handlePridejKontakt(ActionEvent event) {
        String jmeno = txtJmeno.getText().trim();
        String prijmeni = txtPrijmeni.getText().trim();
        String telefon = txtTelefon.getText().trim();
        String email = txtEmail.getText().trim();

        lblStatus.setText(""); // Vyčistit předchozí stav

        if (jmeno.isEmpty() || prijmeni.isEmpty() || telefon.isEmpty() || email.isEmpty()) {
            lblStatus.setText("Chyba: Všechna pole musí být vyplněna.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            lblStatus.setText("Chyba: Neplatný formát emailu.");
            return;
        }

        if (!PHONE_PATTERN.matcher(telefon).matches()) {
            lblStatus.setText("Chyba: Neplatný formát telefonu. Očekávaný formát: (+420) XXX XXX XXX");
            return;
        }

        // Vytvoření a přidání nového kontaktu
        Kontakt novyKontakt = new Kontakt(jmeno, prijmeni, telefon, email);
        seznamKontaktu.add(novyKontakt);

        // Vyčištění formuláře
        txtJmeno.clear();
        txtPrijmeni.clear();
        txtTelefon.clear();
        txtEmail.clear();

        lblStatus.setText("Kontakt '" + jmeno + " " + prijmeni + "' úspěšně přidán.");
    }
}
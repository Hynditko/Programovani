package cz.spse.prj08_javafx_fxml_login;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.util.regex.Pattern;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ContactManagerController {

    // Seznam pro uchování kontaktů
    // --- DATA ---
    private final ObservableList<Kontakt> seznamKontaktu = FXCollections.observableArrayList();
    private String FILE_NAME = "kontakty_default.txt";

    // --- REGEXY ---
    // OPRAVA: Změnil jsem {2,6} na {2,}, aby to bralo i delší domény a zjednodušil kontrolu
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    // Telefon regex necháme pro finální validaci, ale formátování řešíme v Listeneru níže
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+\\d{3} ?)?(\\d{3} ?){2}\\d{3}$");

    // --- LEVÝ PANEL (SEZNAM A PŘIDÁNÍ) ---
    @FXML private TextField txtSearch;
    @FXML private TableView<Kontakt> tableViewKontakty;
    @FXML private TableColumn<Kontakt, String> colJmeno;
    @FXML private TextField txtJmeno;
    @FXML private TextField txtPrijmeni;
    @FXML private TextField txtTelefon;
    @FXML private TextField txtEmail;
    @FXML private Label lblStatus;

    // --- PRAVÝ PANEL (DETAIL A EDITACE) ---
    @FXML private VBox contactDetailPane;

    // Zobrazení (Labels)
    @FXML private Label lblJmenoPrijmeni;
    @FXML private Label lblDetailTelefon;
    @FXML private Label lblDetailEmail;
    @FXML private HBox buttonsViewMode;

    // Editace (TextFields)
    @FXML private HBox editNameBox;
    @FXML private TextField txtEditJmeno;
    @FXML private TextField txtEditPrijmeni;
    @FXML private TextField txtEditTelefon;
    @FXML private TextField txtEditEmail;
    @FXML private HBox buttonsEditMode;

    // --- INITIALIZE ---
    @FXML
    public void initialize() {
        // nactiKontaktyZeSouboru(); // Smazáno, volá se až v nastaveniUzivatele()

        colJmeno.setCellValueFactory(cellData -> {
            Kontakt kontakt = cellData.getValue();
            return new SimpleStringProperty(kontakt.getJmeno() + " " + kontakt.getPrijmeni());
        });

        tableViewKontakty.setItems(seznamKontaktu);
        lblStatus.setText("Připraveno");

        // Aplikace automatického formátování na telefonní pole
        aplikujFormatovaniTelefonu(txtTelefon);
        aplikujFormatovaniTelefonu(txtEditTelefon);

        // Inicializace detailu
        zobrazDetailKontaktu(null);

        tableViewKontakty.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> zobrazDetailKontaktu(newValue)
        );

        FilteredList<Kontakt> filteredData = new FilteredList<>(seznamKontaktu, p -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(kontakt -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return kontakt.getJmeno().toLowerCase().contains(lowerCaseFilter) ||
                        kontakt.getPrijmeni().toLowerCase().contains(lowerCaseFilter) ||
                        kontakt.getTelefon().contains(lowerCaseFilter);
            });
        });
        tableViewKontakty.setItems(filteredData);
    }

    // --- NOVÁ METODA PRO INICIALIZACI KONKRÉTNÍHO UŽIVATELE ---
    public void nastaveniUzivatele(String username) {
        // Vytvoří unikátní název souboru, např. "kontakty_admin.txt"
        this.FILE_NAME = "kontakty_" + username + ".txt";

        // Nyní, když známe soubor, načteme data
        nactiKontaktyZeSouboru();

        // Aktualizujeme status
        lblStatus.setText("Přihlášen uživatel: " + username);
    }

    // --- NOVÁ METODA PRO FORMÁTOVÁNÍ TELEFONU ---
    private void aplikujFormatovaniTelefonu(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            // Povolit jen číslice a mezery
            if (!newValue.matches("[\\d ]*")) {
                field.setText(newValue.replaceAll("[^\\d ]", ""));
                return;
            }

            // Pokud uživatel maže, neformátujeme, aby se "nepral" s kurzorem
            if (newValue.length() < oldValue.length()) {
                return;
            }

            // Odstraníme mezery pro čistá data
            String digits = newValue.replaceAll("\\s", "");

            // Automatické doplňování mezer: 123 456 789
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && i % 3 == 0) {
                    formatted.append(" ");
                }
                formatted.append(digits.charAt(i));
            }

            // Nastavíme jen pokud se liší, abychom nezacyklili listener
            if (!formatted.toString().equals(newValue)) {
                field.setText(formatted.toString());
                field.positionCaret(formatted.length()); // Posun kurzoru na konec
            }
        });
    }

    // --- NOVÁ METODA PRO STATUS ZPRÁVU (MIZÍ PO 3s) ---
    private void zobrazStatus(String text, boolean jeChyba) {
        lblStatus.setText(text);
        // Pokud chceš měnit barvu statusu podle chyby/úspěchu, můžeš zde:
        // if (jeChyba) lblStatus.setStyle("-fx-text-fill: red;");
        // else lblStatus.setStyle("-fx-text-fill: white;");

        // Časovač na smazání textu
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblStatus.setText(""));
        pause.play();
    }

    private void zobrazDetailKontaktu(Kontakt kontakt) {
        toggleEditMode(false);
        if (kontakt != null) {
            lblJmenoPrijmeni.setText(kontakt.getJmeno() + " " + kontakt.getPrijmeni());
            lblDetailTelefon.setText("Telefon: " + kontakt.getTelefon());
            lblDetailEmail.setText("Email: " + kontakt.getEmail());
            contactDetailPane.setVisible(true);
        } else {
            lblJmenoPrijmeni.setText("Vyberte kontakt");
            lblDetailTelefon.setText("");
            lblDetailEmail.setText("");
        }
    }

    private void toggleEditMode(boolean enableEdit) {
        lblJmenoPrijmeni.setVisible(!enableEdit);
        lblJmenoPrijmeni.setManaged(!enableEdit);
        lblDetailTelefon.setVisible(!enableEdit);
        lblDetailTelefon.setManaged(!enableEdit);
        lblDetailEmail.setVisible(!enableEdit);
        lblDetailEmail.setManaged(!enableEdit);
        buttonsViewMode.setVisible(!enableEdit);
        buttonsViewMode.setManaged(!enableEdit);

        editNameBox.setVisible(enableEdit);
        editNameBox.setManaged(enableEdit);
        txtEditTelefon.setVisible(enableEdit);
        txtEditTelefon.setManaged(enableEdit);
        txtEditEmail.setVisible(enableEdit);
        txtEditEmail.setManaged(enableEdit);
        buttonsEditMode.setVisible(enableEdit);
        buttonsEditMode.setManaged(enableEdit);
    }

    // --- AKCE TLAČÍTEK ---

    @FXML
    private void handlePridejKontakt(ActionEvent event) {
        String jmeno = txtJmeno.getText().trim();
        String prijmeni = txtPrijmeni.getText().trim();
        String telefon = txtTelefon.getText().trim();
        String email = txtEmail.getText().trim();

        if (validujUdaje(jmeno, prijmeni, telefon, email)) {
            Kontakt novyKontakt = new Kontakt(jmeno, prijmeni, telefon, email);
            seznamKontaktu.add(novyKontakt);
            ulozKontaktyDoSouboru();

            txtJmeno.clear(); txtPrijmeni.clear(); txtTelefon.clear(); txtEmail.clear();
            zobrazStatus("Kontakt přidán.", false);
        }
    }

    @FXML
    private void handleSmazKontakt() {
        Kontakt vybrany = tableViewKontakty.getSelectionModel().getSelectedItem();
        if (vybrany != null) {
            seznamKontaktu.remove(vybrany);
            ulozKontaktyDoSouboru();
            zobrazDetailKontaktu(null);
            zobrazStatus("Kontakt smazán.", false);
        }
    }

    @FXML
    private void handleUpravKontakt() {
        Kontakt vybrany = tableViewKontakty.getSelectionModel().getSelectedItem();
        if (vybrany != null) {
            txtEditJmeno.setText(vybrany.getJmeno());
            txtEditPrijmeni.setText(vybrany.getPrijmeni());
            txtEditTelefon.setText(vybrany.getTelefon());
            txtEditEmail.setText(vybrany.getEmail());
            toggleEditMode(true);
        }
    }

    @FXML
    private void handleUlozitZmeny() {
        Kontakt vybrany = tableViewKontakty.getSelectionModel().getSelectedItem();
        if (vybrany != null) {
            String noveJmeno = txtEditJmeno.getText().trim();
            String novePrijmeni = txtEditPrijmeni.getText().trim();
            String novyTelefon = txtEditTelefon.getText().trim();
            String novyEmail = txtEditEmail.getText().trim();

            if (validujUdaje(noveJmeno, novePrijmeni, novyTelefon, novyEmail)) {
                vybrany.setJmeno(noveJmeno);
                vybrany.setPrijmeni(novePrijmeni);
                vybrany.setTelefon(novyTelefon);
                vybrany.setEmail(novyEmail);

                ulozKontaktyDoSouboru();
                tableViewKontakty.refresh();
                zobrazDetailKontaktu(vybrany);
                zobrazStatus("Změny uloženy.", false);
            }
        }
    }

    @FXML
    private void handleZrusitUpravy() {
        toggleEditMode(false);
    }

    // --- VALIDACE ---
    // Upraveno: Label se už nepředává, voláme novou metodu zobrazStatus()
    private boolean validujUdaje(String j, String p, String t, String e) {
        if (j.isEmpty() || p.isEmpty() || t.isEmpty() || e.isEmpty()) {
            zobrazStatus("Chyba: Všechna pole musí být vyplněna.", true);
            return false;
        }
        if (!EMAIL_PATTERN.matcher(e).matches()) {
            zobrazStatus("Chyba: Neplatný formát emailu.", true);
            return false;
        }
        // Telefon kontrolujeme, zda sedí na regex (formátování už zajistil listener)
        if (!PHONE_PATTERN.matcher(t).matches()) {
            zobrazStatus("Chyba tel: 777 111 222 nebo +420...", true);
            return false;
        }
        return true;
    }

    private void ulozKontaktyDoSouboru() {
        // Použijeme metodu pro získání správné cesty k souboru uživatele
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getDatovySoubor(FILE_NAME)))) {
            for (Kontakt k : seznamKontaktu) {
                writer.write(k.getJmeno() + ";" + k.getPrijmeni() + ";" + k.getEmail() + ";" + k.getTelefon());
            }
        } catch (IOException e) {
            e.printStackTrace();
            zobrazStatus("Chyba při ukládání souboru.", true);
        }
    }

    private void nactiKontaktyZeSouboru() {
        // Načítáme ze souboru v bezpečné složce
        File soubor = getDatovySoubor(FILE_NAME);

        if (!soubor.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(soubor), StandardCharsets.UTF_8))) {
            String radek;
            while ((radek = reader.readLine()) != null) {
                String[] d = radek.split(";");
                if (d.length == 4) {
                    seznamKontaktu.add(new Kontakt(d[0], d[1], d[2], d[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Pomocná metoda pro validaci, kterou nyní volá Přidat i Upravit
    private boolean validujVstupy() {
        // Zde vložte ty vaše kontroly (EMAIL_PATTERN, PHONE_PATTERN atd.)
        // Pokud je vše OK, return true, jinak false
        return true;
    }

    private void handleKontaktVyber(ObservableValue<? extends Kontakt> observable, Kontakt oldValue, Kontakt newValue) {
        // ... (kód uvnitř metody) ...
        if (newValue != null) {
            // Zde už se to jen spojuje, což je OK
            lblJmenoPrijmeni.setText(newValue.getJmeno() + " " + newValue.getPrijmeni());
            lblDetailTelefon.setText(newValue.getTelefon());
            lblDetailEmail.setText(newValue.getEmail());
        }
        // ...
    }
    // Metoda pro získání cesty k datové složce v profilu uživatele
    private File getDatovySoubor(String nazevSouboru) {
        String userHome = System.getProperty("user.home");
        File dataDir = new File(userHome, ".contactmanager");

        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        return new File(dataDir, nazevSouboru);
    }
}
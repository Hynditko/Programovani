package cz.spse.prj08_javafx_fxml_login;

import javafx.beans.property.SimpleStringProperty;

public class Kontakt {
    private final SimpleStringProperty jmeno;
    private final SimpleStringProperty prijmeni;
    private final SimpleStringProperty telefon;
    private final SimpleStringProperty email;

    public Kontakt(String jmeno, String prijmeni, String telefon, String email) {
        this.jmeno = new SimpleStringProperty(jmeno);
        this.prijmeni = new SimpleStringProperty(prijmeni);
        this.telefon = new SimpleStringProperty(telefon);
        this.email = new SimpleStringProperty(email);
    }

    // Gettery a settery pro JavaFX TableView
    public String getJmeno() {
        return jmeno.get();
    }

    public SimpleStringProperty jmenoProperty() {
        return jmeno;
    }

    public void setJmeno(String jmeno) {
        this.jmeno.set(jmeno);
    }

    public String getPrijmeni() {
        return prijmeni.get();
    }

    public SimpleStringProperty prijmeniProperty() {
        return prijmeni;
    }

    public void setPrijmeni(String prijmeni) {
        this.prijmeni.set(prijmeni);
    }

    public String getTelefon() {
        return telefon.get();
    }

    public SimpleStringProperty telefonProperty() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon.set(telefon);
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    @Override
    public String toString() {
        return getJmeno() + " " + getPrijmeni() + " (" + getTelefon() + ", " + getEmail() + ")";
    }
}

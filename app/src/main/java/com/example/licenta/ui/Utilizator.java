package com.example.licenta.ui;

public class Utilizator {
    private String nume;
    private String prenume;
    private String telefon;
    private int varsta;
    private String afectiuni;
    private String email;
    private String parola;
    private String numeContact;
    private String prenumeContact;
    private String telefonContact;

    private String uid;

    public Utilizator() {
        // Constructor gol necesar pentru Firebase
    }

    public Utilizator(String nume, String prenume, String telefon, int varsta, String afectiuni, String email, String parola) {
        this.nume = nume;
        this.prenume = prenume;
        this.telefon = telefon;
        this.varsta = varsta;
        this.afectiuni = afectiuni;
        this.email = email;
        this.parola = parola;
        this.numeContact = "";
        this.prenumeContact = "";
        this.telefonContact = "";
    }


    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public int getVarsta() {
        return varsta;
    }

    public void setVarsta(int varsta) {
        this.varsta = varsta;
    }

    public String getAfectiuni() {
        return afectiuni;
    }

    public void setAfectiuni(String afectiuni) {
        this.afectiuni = afectiuni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public String getNumeContact() {
        return numeContact;
    }

    public String getUid(){return uid;}

    public void setNumeContact(String numeContact) {
        this.numeContact = numeContact;
    }

    public String getPrenumeContact() {
        return prenumeContact;
    }

    public void setPrenumeContact(String prenumeContact) {
        this.prenumeContact = prenumeContact;
    }

    public String getTelefonContact() {
        return telefonContact;
    }

    public void setTelefonContact(String telefonContact) {
        this.telefonContact = telefonContact;
    }

    public void setUid(String uid) {
        this.uid=uid;
    }
}

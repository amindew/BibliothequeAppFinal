package com.example.bibliothequeapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "livres")
public class Livre implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String titre;
    private String auteur;
    private String isbn;
    private boolean disponible;
    private String imageUri;
    private int anneePublication; // ← nouveau champ

    public Livre(int id, String titre, String auteur, String isbn,
                 boolean disponible, String imageUri, int anneePublication) {
        this.id = id;
        this.titre = titre;
        this.auteur = auteur;
        this.isbn = isbn;
        this.disponible = disponible;
        this.imageUri = imageUri;
        this.anneePublication = anneePublication;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public int getAnneePublication() { return anneePublication; }
    public void setAnneePublication(int anneePublication) { this.anneePublication = anneePublication; }
}
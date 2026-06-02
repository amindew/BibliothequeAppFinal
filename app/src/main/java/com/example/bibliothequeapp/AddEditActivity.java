package com.example.bibliothequeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditActivity extends AppCompatActivity {

    public static final String EXTRA_MODE  = "MODE";
    public static final String EXTRA_LIVRE = "LIVRE";
    public static final String MODE_ADD    = "ADD";
    public static final String MODE_EDIT   = "EDIT";

    private EditText etTitre, etAuteur, etIsbn, etAnneePublication;
    private Switch switchDisponible;
    private Button btnEnregistrer, btnChoisirImage;
    private ImageButton btnRetour;
    private TextView tvTitreFormulaire;
    private ImageView imgApercuCouverture;

    private String mode;
    private Livre livreAModifier;
    private String imageUriSelectionnee = null;

    private AppDatabase database;
    private ExecutorService executorService;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        database        = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        tvTitreFormulaire   = findViewById(R.id.tvTitreFormulaire);
        etTitre             = findViewById(R.id.etTitre);
        etAuteur            = findViewById(R.id.etAuteur);
        etIsbn              = findViewById(R.id.etIsbn);
        etAnneePublication  = findViewById(R.id.etAnneePublication);
        switchDisponible    = findViewById(R.id.switchDisponible);
        btnEnregistrer      = findViewById(R.id.btnEnregistrer);
        btnChoisirImage     = findViewById(R.id.btnChoisirImage);
        btnRetour           = findViewById(R.id.btnRetour);
        imgApercuCouverture = findViewById(R.id.imgApercuCouverture);

        btnRetour.setOnClickListener(v -> finish());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        imageUriSelectionnee = uri.toString();
                        imgApercuCouverture.setImageURI(uri);
                    }
                }
        );

        btnChoisirImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        mode = getIntent().getStringExtra(EXTRA_MODE);

        if (MODE_EDIT.equals(mode)) {
            tvTitreFormulaire.setText("Modifier le livre");
            livreAModifier = (Livre) getIntent().getSerializableExtra(EXTRA_LIVRE);

            if (livreAModifier != null) {
                etTitre.setText(livreAModifier.getTitre());
                etAuteur.setText(livreAModifier.getAuteur());
                etIsbn.setText(livreAModifier.getIsbn());
                switchDisponible.setChecked(livreAModifier.isDisponible());

                if (livreAModifier.getAnneePublication() > 0) {
                    etAnneePublication.setText(
                            String.valueOf(livreAModifier.getAnneePublication()));
                }

                if (livreAModifier.getImageUri() != null) {
                    imageUriSelectionnee = livreAModifier.getImageUri();
                    imgApercuCouverture.setImageURI(Uri.parse(imageUriSelectionnee));
                }
            }
        } else {
            mode = MODE_ADD;
            tvTitreFormulaire.setText("Ajouter un livre");
        }

        btnEnregistrer.setOnClickListener(v -> enregistrerLivre());
    }

    private void enregistrerLivre() {
        String titre  = etTitre.getText().toString().trim();
        String auteur = etAuteur.getText().toString().trim();
        String isbn   = etIsbn.getText().toString().trim();
        boolean disponible = switchDisponible.isChecked();

        int anneePublication = 0;
        String anneeStr = etAnneePublication.getText().toString().trim();
        if (!anneeStr.isEmpty()) {
            try {
                anneePublication = Integer.parseInt(anneeStr);
            } catch (NumberFormatException e) {
                etAnneePublication.setError("Année invalide");
                return;
            }
        }

        if (!validerFormulaire(titre, auteur, isbn)) return;

        Livre livre;
        if (MODE_EDIT.equals(mode) && livreAModifier != null) {
            livre = new Livre(
                    livreAModifier.getId(), titre, auteur, isbn, disponible,
                    imageUriSelectionnee != null ? imageUriSelectionnee : livreAModifier.getImageUri(),
                    anneePublication
            );
        } else {
            livre = new Livre(0, titre, auteur, isbn, disponible,
                    imageUriSelectionnee, anneePublication);
        }

        // ✅ Sauvegarde directement dans Room ici
        executorService.execute(() -> {
            if (MODE_EDIT.equals(mode)) {
                database.livreDao().update(livre);
            } else {
                livre.setId(0);
                database.livreDao().insert(livre);
            }
            runOnUiThread(() -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_MODE, mode);
                resultIntent.putExtra(EXTRA_LIVRE, livre);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        });
    }

    private boolean validerFormulaire(String titre, String auteur, String isbn) {
        boolean valide = true;
        if (TextUtils.isEmpty(titre)) {
            etTitre.setError("Le titre est obligatoire");
            valide = false;
        }
        if (TextUtils.isEmpty(auteur)) {
            etAuteur.setError("L'auteur est obligatoire");
            valide = false;
        }
        if (!TextUtils.isEmpty(isbn) && isbn.length() < 10) {
            etIsbn.setError("L'ISBN doit contenir au moins 10 caractères");
            valide = false;
        }
        return valide;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}
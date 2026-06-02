package com.example.bibliothequeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {

    TextView tvTitre, tvAuteur, tvIsbn, tvDisponibilite, tvAnneePublication;
    ImageButton btnRetour;
    ImageView imgCouverture;
    Button btnModifier, btnSupprimer;
    private Livre livreActuel;

    private AppDatabase database;
    private ExecutorService executorService;

    // ✅ FIX : on utilise un launcher pour que la modification
    //    remonte correctement le résultat à MainActivity
    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        database        = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        btnRetour          = findViewById(R.id.btnRetour);
        tvTitre            = findViewById(R.id.tvTitre);
        tvAuteur           = findViewById(R.id.tvAuteur);
        tvIsbn             = findViewById(R.id.tvIsbn);
        tvDisponibilite    = findViewById(R.id.tvDisponibilite);
        tvAnneePublication = findViewById(R.id.tvAnneePublication);
        imgCouverture      = findViewById(R.id.imgCouvertureDetail);
        btnModifier        = findViewById(R.id.btnModifier);
        btnSupprimer       = findViewById(R.id.btnSupprimer);

        btnRetour.setOnClickListener(v -> finish());

        livreActuel = (Livre) getIntent().getSerializableExtra("livre");

        // Sécurité : si le livre est null on ferme proprement
        if (livreActuel == null) {
            finish();
            return;
        }

        afficherLivre(livreActuel);

        // ✅ FIX : le launcher reçoit le livre modifié, relaie RESULT_OK
        //    à MainActivity qui rafraîchira la liste automatiquement
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
        );

        btnModifier.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, AddEditActivity.class);
            intent.putExtra(AddEditActivity.EXTRA_MODE, AddEditActivity.MODE_EDIT);
            intent.putExtra(AddEditActivity.EXTRA_LIVRE, livreActuel);
            editLauncher.launch(intent);   // ✅ launch, PAS startActivity
        });

        btnSupprimer.setOnClickListener(v -> confirmerSuppression());
    }

    private void afficherLivre(Livre livre) {
        tvTitre.setText(livre.getTitre());
        tvAuteur.setText("Auteur : " + livre.getAuteur());
        tvIsbn.setText("ISBN : " + livre.getIsbn());
        tvDisponibilite.setText(livre.isDisponible() ? "Disponible" : "Indisponible");

        // Afficher l'année seulement si elle est renseignée
        if (livre.getAnneePublication() > 0) {
            tvAnneePublication.setText("Année : " + livre.getAnneePublication());
        } else {
            tvAnneePublication.setText("");
        }

        // ✅ Logo de l'appli par défaut si aucune photo choisie
        if (livre.getImageUri() != null && !livre.getImageUri().isEmpty()) {
            imgCouverture.setImageURI(Uri.parse(livre.getImageUri()));
        } else {
            imgCouverture.setImageResource(R.drawable.mon_logoo); // logo de l'appli
        }
    }

    private void confirmerSuppression() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le livre")
                .setMessage("Voulez-vous vraiment supprimer ce livre ?")
                .setPositiveButton("Supprimer", (dialog, which) -> supprimerLivre())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerLivre() {
        executorService.execute(() -> {
            database.livreDao().delete(livreActuel);
            runOnUiThread(() -> {
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}
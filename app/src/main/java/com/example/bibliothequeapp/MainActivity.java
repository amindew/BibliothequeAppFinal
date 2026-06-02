package com.example.bibliothequeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewLivres;
    private FloatingActionButton fabAjouterLivre;
    private TextView tvListeVide;
    private EditText etRecherche;

    private LivreAdapter livreAdapter;
    private List<Livre> listeLivres;

    private AppDatabase database;
    private ExecutorService executorService;

    private ActivityResultLauncher<Intent> addEditLauncher;
    private ActivityResultLauncher<Intent> detailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerViewLivres = findViewById(R.id.recyclerViewLivres);
        fabAjouterLivre    = findViewById(R.id.fabAjouterLivre);
        tvListeVide        = findViewById(R.id.tvListeVide);
        etRecherche        = findViewById(R.id.etRecherche);

        database        = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        listeLivres     = new ArrayList<>();

        livreAdapter = new LivreAdapter(listeLivres, new LivreAdapter.OnLivreClickListener() {
            @Override
            public void onLivreClick(Livre livre) {
                ouvrirDetailLivre(livre);
            }

            @Override
            public void onModifierClick(Livre livre, int position) {
                ouvrirFormulaireModification(livre);
            }

            @Override
            public void onSupprimerClick(Livre livre, int position) {
                confirmerSuppression(livre, position);
            }
        });

        recyclerViewLivres.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLivres.setAdapter(livreAdapter);

        initialiserLaunchers();
        fabAjouterLivre.setOnClickListener(v -> ouvrirFormulaireAjout());
        initialiserRecherche();
        chargerLivresDepuisRoom();
    }

    private void initialiserLaunchers() {

        // Launcher Ajout / Modification — Room déjà sauvegardé dans AddEditActivity
        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        chargerLivresDepuisRoom();
                        Toast.makeText(this, "Livre enregistré ✓", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher DetailActivity — recharge si modification ou suppression
        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        chargerLivresDepuisRoom();
                    }
                }
        );
    }

    private void initialiserRecherche() {
        etRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String motCle = s.toString().trim();
                if (motCle.isEmpty()) chargerLivresDepuisRoom();
                else rechercherLivres(motCle);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void rechercherLivres(String motCle) {
        executorService.execute(() -> {
            List<Livre> resultats = database.livreDao().rechercherParTitre(motCle);
            runOnUiThread(() -> {
                livreAdapter.mettreAJourListe(resultats);
                mettreAJourListeVide();
            });
        });
    }

    private void chargerLivresDepuisRoom() {
        executorService.execute(() -> {
            List<Livre> livresDepuisBase = database.livreDao().getAllLivres();
            runOnUiThread(() -> {
                livreAdapter.mettreAJourListe(livresDepuisBase);
                mettreAJourListeVide();
            });
        });
    }

    private void mettreAJourListeVide() {
        if (listeLivres.isEmpty()) {
            tvListeVide.setVisibility(View.VISIBLE);
            recyclerViewLivres.setVisibility(View.GONE);
        } else {
            tvListeVide.setVisibility(View.GONE);
            recyclerViewLivres.setVisibility(View.VISIBLE);
        }
    }

    private void supprimerLivreDansRoom(Livre livre, int position) {
        executorService.execute(() -> {
            database.livreDao().delete(livre);
            runOnUiThread(() -> {
                livreAdapter.supprimerLivre(position);
                mettreAJourListeVide();
                Toast.makeText(this, "Livre supprimé", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void ouvrirFormulaireAjout() {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(AddEditActivity.EXTRA_MODE, AddEditActivity.MODE_ADD);
        addEditLauncher.launch(intent);
    }

    private void ouvrirFormulaireModification(Livre livre) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(AddEditActivity.EXTRA_MODE, AddEditActivity.MODE_EDIT);
        intent.putExtra(AddEditActivity.EXTRA_LIVRE, livre);
        addEditLauncher.launch(intent);
    }

    private void ouvrirDetailLivre(Livre livre) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("livre", livre);
        detailLauncher.launch(intent);
    }

    private void confirmerSuppression(Livre livre, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer « " + livre.getTitre() + " » ?")
                .setMessage("Cette action est irréversible.")
                .setPositiveButton("Supprimer", (d, w) -> supprimerLivreDansRoom(livre, position))
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}
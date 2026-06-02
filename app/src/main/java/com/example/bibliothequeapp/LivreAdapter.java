package com.example.bibliothequeapp;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LivreAdapter extends RecyclerView.Adapter<LivreAdapter.LivreViewHolder> {

    public interface OnLivreClickListener {
        void onLivreClick(Livre livre);
        void onModifierClick(Livre livre, int position);
        void onSupprimerClick(Livre livre, int position);
    }

    private List<Livre> listeLivres;
    private OnLivreClickListener listener;

    public LivreAdapter(List<Livre> listeLivres, OnLivreClickListener listener) {
        this.listeLivres = listeLivres;
        this.listener    = listener;
    }

    @NonNull
    @Override
    public LivreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_livre, parent, false);
        return new LivreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LivreViewHolder holder, int position) {
        Livre livre = listeLivres.get(position);

        holder.tvTitreLivre.setText(livre.getTitre());
        holder.tvAuteurLivre.setText("Auteur : " + livre.getAuteur());
        holder.tvIsbnLivre.setText("ISBN : " + livre.getIsbn());

        // Année (cachée si non renseignée)
        if (livre.getAnneePublication() > 0) {
            holder.tvAnneeLivre.setVisibility(View.VISIBLE);
            holder.tvAnneeLivre.setText("Année : " + livre.getAnneePublication());
        } else {
            holder.tvAnneeLivre.setVisibility(View.GONE);
        }

        // ✅ Logo de l'appli par défaut si aucune couverture choisie
        if (livre.getImageUri() != null && !livre.getImageUri().isEmpty()) {
            holder.imgCouverture.setImageURI(Uri.parse(livre.getImageUri()));
        } else {
            holder.imgCouverture.setImageResource(R.drawable.mon_logoo);
        }

        // Badge disponibilité
        if (livre.isDisponible()) {
            holder.tvDisponibilite.setText("Disponible");
            holder.tvDisponibilite.setBackgroundResource(R.drawable.badge_arrondi);
            holder.tvDisponibilite.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#2E7D32")));
        } else {
            holder.tvDisponibilite.setText("Indisponible");
            holder.tvDisponibilite.setBackgroundResource(R.drawable.badge_arrondi);
            holder.tvDisponibilite.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#C62828")));
        }

        // Clic carte → détail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLivreClick(livre);
        });

        // Icône ✏️ modifier
        holder.btnModifier.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                    listener.onModifierClick(livre, pos);
            }
        });

        // Icône 🗑️ supprimer
        holder.btnSupprimer.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                    listener.onSupprimerClick(livre, pos);
            }
        });
    }

    @Override
    public int getItemCount() { return listeLivres.size(); }

    // ── Mises à jour granulaires ──────────────────────────────────────

    public void ajouterLivre(Livre livre) {
        listeLivres.add(0, livre);
        notifyItemInserted(0);
    }

    public void modifierLivre(Livre livre, int position) {
        listeLivres.set(position, livre);
        notifyItemChanged(position);
    }

    public void supprimerLivre(int position) {
        listeLivres.remove(position);
        notifyItemRemoved(position);
    }

    public void mettreAJourListe(List<Livre> nouvelleListe) {
        listeLivres.clear();
        listeLivres.addAll(nouvelleListe);
        notifyDataSetChanged();
    }

    // ── ViewHolder ────────────────────────────────────────────────────

    public static class LivreViewHolder extends RecyclerView.ViewHolder {

        TextView  tvTitreLivre, tvAuteurLivre, tvIsbnLivre, tvDisponibilite, tvAnneeLivre;
        ImageView imgCouverture;
        ImageButton btnModifier, btnSupprimer;

        public LivreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitreLivre    = itemView.findViewById(R.id.tvTitreLivre);
            tvAuteurLivre   = itemView.findViewById(R.id.tvAuteurLivre);
            tvIsbnLivre     = itemView.findViewById(R.id.tvIsbnLivre);
            tvDisponibilite = itemView.findViewById(R.id.tvDisponibilite);
            tvAnneeLivre    = itemView.findViewById(R.id.tvAnneeLivre);
            imgCouverture   = itemView.findViewById(R.id.mon_logoo);
            btnModifier     = itemView.findViewById(R.id.btnModifierItem);
            btnSupprimer    = itemView.findViewById(R.id.btnSupprimerItem);
        }
    }
}
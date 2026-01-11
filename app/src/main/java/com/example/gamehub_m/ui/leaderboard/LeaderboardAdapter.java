package com.example.gamehub_m.ui.leaderboard;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamehub_m.R;
import com.example.gamehub_m.data.ScoreManager;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<ScoreManager.LeaderboardEntry> entries = new ArrayList<>();

    public void setEntries(List<ScoreManager.LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScoreManager.LeaderboardEntry entry = entries.get(position);
        int rank = position + 1;
        holder.bind(entry, rank);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView rankText;
        private final TextView nameText;
        private final TextView scoreText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rankText);
            nameText = itemView.findViewById(R.id.nameText);
            scoreText = itemView.findViewById(R.id.scoreText);
        }

        public void bind(ScoreManager.LeaderboardEntry entry, int rank) {
            rankText.setText(String.valueOf(rank));
            nameText.setText(entry.displayName);
            scoreText.setText(String.valueOf(entry.highScore));

            // Rank styling
            GradientDrawable background = (GradientDrawable) rankText.getBackground();
            
            if (rank == 1) {
                background.setColor(Color.parseColor("#FFD700")); // Gold
                rankText.setTextColor(Color.BLACK);
            } else if (rank == 2) {
                background.setColor(Color.parseColor("#C0C0C0")); // Silver
                rankText.setTextColor(Color.BLACK);
            } else if (rank == 3) {
                background.setColor(Color.parseColor("#CD7F32")); // Bronze
                rankText.setTextColor(Color.BLACK);
            } else {
                background.setColor(Color.parseColor("#3A3A4E")); // Default Dark
                rankText.setTextColor(Color.WHITE);
            }
        }
    }
}

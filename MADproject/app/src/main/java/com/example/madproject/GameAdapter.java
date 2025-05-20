package com.example.madproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
    private List<Game> games;
    private OnGameClickListener listener;
    private OnAddToCartListener addToCartListener;
    private OnRemoveListener removeListener;
    private boolean isExploreView = false;

    public interface OnGameClickListener {
        void onGameClick(Game game);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Game game);
    }

    public interface OnRemoveListener {
        void onRemove(Game game);
    }

    public void setOnAddToCartListener(OnAddToCartListener listener) {
        this.addToCartListener = listener;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.removeListener = listener;
    }

    public GameAdapter(List<Game> games, OnGameClickListener listener, boolean isExploreView) {
        this.games = games;
        this.listener = listener;
        this.isExploreView = isExploreView;
    }

    public GameAdapter(List<Game> games, OnGameClickListener listener) {
        this.games = games;
        this.listener = listener;
        this.isExploreView = false;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bind(game);

        if (isExploreView) {
            holder.btnRemove.setVisibility(View.GONE);
        } else {
            holder.btnRemove.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    class GameViewHolder extends RecyclerView.ViewHolder {
        private ImageView gameImage;
        private TextView gameName;
        private RatingBar gameRating;
        private Button btnAddToCart;
        private Button btnRemove;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameImage = itemView.findViewById(R.id.gameImage);
            gameName = itemView.findViewById(R.id.gameName);
            gameRating = itemView.findViewById(R.id.gameRating);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onGameClick(games.get(position));
                }
            });
            btnAddToCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && addToCartListener != null) {
                    addToCartListener.onAddToCart(games.get(position));
                }
            });
            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemove(games.get(position));
                }
            });
        }

        public void bind(Game game) {
            gameName.setText(game.getName());
            gameRating.setRating((float) game.getRating());

            Glide.with(itemView.getContext())
                    .load(game.getImageUrl())
                    .centerCrop()
                    .into(gameImage);
        }
    }
}
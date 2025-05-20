package com.example.madproject;

import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.madproject.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private GameAdapter gameAdapter;
    private List<Game> cartList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("My Cart");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        cartList = new ArrayList<>();
        gameAdapter = new GameAdapter(cartList, game -> {
        });
        gameAdapter.setOnRemoveListener(this::removeFromCart);
        gameAdapter.setOnAddToCartListener(this::buyGame);
        binding.gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.gamesRecyclerView.setAdapter(gameAdapter);

        loadCart();
    }

    private void loadCart() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabase.child("users").child(userId).child("cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot gameSnap : snapshot.getChildren()) {
                    Game game = gameSnap.getValue(Game.class);
                    if (game != null)
                        cartList.add(game);
                }
                gameAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromCart(Game game) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            Log.w("CartActivity", "removeFromCart: User not logged in");
            return;
        }
        Log.d("CartActivity", "Attempting to remove game from cart: " + game.getName() + " for user: " + userId);
        mDatabase.child("users").child(userId).child("cart").child(String.valueOf(game.getId())).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Removed from cart", Toast.LENGTH_SHORT).show();
                    Log.d("CartActivity", "Game removed from cart successfully: " + game.getName());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove from cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CartActivity",
                            "Failed to remove game from cart: " + game.getName() + ", Error: " + e.getMessage());
                });
    }

    private void buyGame(Game game) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            Log.w("CartActivity", "buyGame: User not logged in");
            return;
        }
        Log.d("CartActivity", "Attempting to buy game: " + game.getName() + " for user: " + userId);

        mDatabase.child("users").child(userId).child("library").child(String.valueOf(game.getId())).setValue(game)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Game bought and added to library", Toast.LENGTH_SHORT).show();
                    Log.d("CartActivity", "Game bought and added to library successfully: " + game.getName());

                    removeFromCart(game);
                })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(this, "Failed to buy game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("CartActivity",
                                    "Failed to buy game: " + game.getName() + ", Error: " + e.getMessage());
                        });
    }
}
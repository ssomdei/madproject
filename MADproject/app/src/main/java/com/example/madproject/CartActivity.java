package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.madproject.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private String USER_KEY = "users";
    private String CART_KEY = "cart";
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


        binding.bottomNavigation.setSelectedItemId(R.id.navigation_cart);
        binding.bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);

        loadCart();
    }

    private void loadCart() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child(USER_KEY)
                .child(userId)
                .child(CART_KEY)
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                            cartList.clear();
                            for (DataSnapshot gameSnap : task.getResult().getChildren()) {
                                try {
                                    Game game = gameSnap.getValue(Game.class);
                                    if (game != null) {
                                        cartList.add(game);
                                    }
                                } catch (Exception e) {

                                }

                            }

                            binding.progressBar.setVisibility(View.GONE);
                            binding.gamesRecyclerView.setVisibility(View.VISIBLE);

                            if (cartList.isEmpty()) {
                                binding.noItemText.setVisibility(View.VISIBLE);
                            } else {
                                binding.noItemText.setVisibility(View.GONE);
                            }

                            gameAdapter.notifyDataSetChanged();
                        }
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

        gameAdapter.notifyDataSetChanged();
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

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_explore) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (itemId == R.id.navigation_library) {
            startActivity(new Intent(this, LibraryActivity.class));
            finish();
        } else if (itemId == R.id.navigation_cart) {
            return true;
        } else if (itemId == R.id.navigation_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }

        return false;
    }
}
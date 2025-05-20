package com.example.madproject;

import android.os.Bundle;
import android.widget.Toast;

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

public class LibraryActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private GameAdapter gameAdapter;
    private List<Game> libraryList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("My Library");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        libraryList = new ArrayList<>();
        gameAdapter = new GameAdapter(libraryList, game -> {
        });
        gameAdapter.setOnRemoveListener(this::removeFromLibrary);
        gameAdapter.setOnAddToCartListener(null);
        binding.gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.gamesRecyclerView.setAdapter(gameAdapter);

        loadLibrary();
    }

    private void loadLibrary() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabase.child("users").child(userId).child("library").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                libraryList.clear();
                for (DataSnapshot gameSnap : snapshot.getChildren()) {
                    Game game = gameSnap.getValue(Game.class);
                    if (game != null)
                        libraryList.add(game);
                }
                gameAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LibraryActivity.this, "Failed to load library", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromLibrary(Game game) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabase.child("users").child(userId).child("library").child(String.valueOf(game.getId())).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Removed from library", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast
                        .makeText(this, "Failed to remove from library: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
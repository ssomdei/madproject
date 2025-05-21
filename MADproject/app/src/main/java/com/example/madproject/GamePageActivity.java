package com.example.madproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.madproject.databinding.ActivityGamePageBinding;
import com.example.madproject.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class GamePageActivity extends AppCompatActivity {
    private ActivityGamePageBinding binding;
    private GameAdapter gameAdapter;
    private List<Game> libraryList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGamePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("My Library");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        libraryList = new ArrayList<>();
        gameAdapter = new GameAdapter(libraryList, game -> {

        });

        Game game = (Game) getIntent().getSerializableExtra("gameData");

        if (game != null) {
            Log.d("GamePageActivity", "Name: " + game.getName());
        }

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GamePageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loadGame(game);
    }

    private void loadGame(Game game){
        Log.d("june", "loadGame: " + game.getName());

        Glide.with(getApplicationContext())
                .load(game.getImageUrl())
                .centerCrop()
                .into(binding.gameImage);

        binding.gameRating.setRating((float) game.getRating());
        binding.gameNameText.setText(game.getName());
//
        String htmlString = game.getRequirements(); // example: from JSON like "<strong>Minimum:</strong> OS: Windows 10"
        Spanned spanned = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        binding.gameRequirements.setText("Requirements: \n" + spanned);
        binding.gameGenre.setText("Genre: " + game.getGenre());

    }

}

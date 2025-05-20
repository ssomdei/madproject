package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.madproject.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.SearchView;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private String USER_KEY = "users";
    private String CART_KEY = "cart";
    private String NAME_KEY = "name";
    private String BACKGROUND_IMAGE_KEY = "background_image";
    private String RATING_KEY = "rating";
    private String ID_KEY = "id";
    private ActivityMainBinding binding;
    private GameAdapter gameAdapter;
    private List<Game> gamesList;
    private RequestQueue requestQueue;
    private static final String RAWG_API_KEY = "97f11c284a934098af9c0162ee716737"; // Replace with your API key
    private static final String RAWG_BASE_URL = "https://api.rawg.io/api/games";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Explicitly initialize Firebase App
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        // Initialize Firebase
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize RecyclerView
        gamesList = new ArrayList<>();
        gameAdapter = new GameAdapter(gamesList, this::onGameClick, true);
        gameAdapter.setOnAddToCartListener(this::addToCart);
        gameAdapter.setOnRemoveListener(null);
        binding.gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.gamesRecyclerView.setAdapter(gameAdapter);
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_explore);

        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this);

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Load initial games
        loadGames();
    }

    private void loadGames() {
        String url = RAWG_BASE_URL + "?key=" + RAWG_API_KEY + "&page_size=20";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        gamesList.clear();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject gameJson = results.getJSONObject(i);
                            Game game = new Game(
                                    gameJson.getString("name"),
                                    gameJson.getString("background_image"),
                                    gameJson.getDouble("rating"),
                                    gameJson.getInt("id"));
                            gamesList.add(game);
                        }

                        binding.progressBar.setVisibility(View.GONE);
                        binding.gamesRecyclerView.setVisibility(View.VISIBLE);

                        if (gamesList.isEmpty()) {
                            binding.noItemText.setVisibility(View.VISIBLE);
                        } else {
                            binding.noItemText.setVisibility(View.GONE);
                        }

                        gameAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing game data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error loading games", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void loadGames(String query) {
        String url = RAWG_BASE_URL + "?key=" + RAWG_API_KEY + "&page_size=20";
        if (query != null && !query.isEmpty()) {
            url += "&search=" + query;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        gamesList.clear();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject gameJson = results.getJSONObject(i);
                            Game game = new Game(
                                    gameJson.getString("name"),
                                    gameJson.getString("background_image"),
                                    gameJson.getDouble("rating"),
                                    gameJson.getInt("id"));
                            gamesList.add(game);
                        }

                        gameAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing game data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error loading games", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void onGameClick(Game game) {

        Toast.makeText(this, "Selected: " + game.getName(), Toast.LENGTH_SHORT).show();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_explore) {
            return true;
        } else if (itemId == R.id.navigation_library) {
            startActivity(new Intent(this, LibraryActivity.class));
            finish();
        } else if (itemId == R.id.navigation_cart) {
            startActivity(new Intent(this, CartActivity.class));
            finish();
        } else if (itemId == R.id.navigation_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }

        return false;
    }

    private void addToCart(Game game) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            Log.w("MainActivity", "addToCart: User not logged in");
            return;
        }
        Log.d("MainActivity", "Attempting to add game to cart: " + game.getName() + " for user: " + userId);

        mDatabase.child(USER_KEY)
                .child(userId)
                .child(CART_KEY)
                .child(String.valueOf(game.getId()))
                .setValue(game, (error, ref) -> {
                    Log.d("MainActivity", "done adding game");
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        loadGames(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
            loadGames();
        }
        return false;
    }
}
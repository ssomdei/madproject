package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.madproject.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        binding.registerButton.setOnClickListener(v -> {
            String username = binding.usernameInput.getText().toString().trim();
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();
            String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            // Create user data in Realtime Database
                            Map<String, Object> user = new HashMap<>();
                            user.put("username", username);
                            user.put("email", email);
                            user.put("library", new HashMap<>());
                            user.put("cart", new HashMap<>());

                            mDatabase.child("users").child(userId).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Registration successful",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this,
                                            "Failed to save user data: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        binding.loginLink.setOnClickListener(v -> {
            finish();
        });
    }
}
package com.example.madproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView emailText = findViewById(R.id.profileEmail);
        Button logoutButton = findViewById(R.id.logoutButton);

        String email = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : "No email";
        emailText.setText(email);

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        findViewById(R.id.changePass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    private void showChangePasswordDialog() {
        // Inflate the dialog view
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        final EditText currentPassword = dialogView.findViewById(R.id.currentPassword);
        final EditText newPassword = dialogView.findViewById(R.id.newPassword);
        final EditText confirmPassword = dialogView.findViewById(R.id.confirmPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentPass = currentPassword.getText().toString();
                        String newPass = newPassword.getText().toString();
                        String confirmPass = confirmPassword.getText().toString();

                        if (newPass.equals(confirmPass)) {
                            changePassword(currentPass, newPass);
                        } else {
                            Toast.makeText(ProfileActivity.this, "New passwords don't match", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        if (currentUser != null) {
            // Get the current user's email
            String email = currentUser.getEmail();

            if (email != null) {
                // Re-authenticate user to change password using email and current password
                AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Now update the password
                                currentUser.updatePassword(newPassword)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(ProfileActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "Error updating password", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(ProfileActivity.this, "Error: No email found for user", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

}

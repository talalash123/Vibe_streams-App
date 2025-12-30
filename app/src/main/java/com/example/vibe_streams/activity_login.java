package com.example.vibe_streams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Hide Google-related UI as it is not being used
        LinearLayout btnGoogle = findViewById(R.id.btnGoogle);
        TextView orText = findViewById(R.id.orText);
        if (btnGoogle != null) {
            btnGoogle.setVisibility(View.GONE);
        }
        if (orText != null) {
            orText.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Check if the user is attempting to log in as admin
        if ("admin@vibestreams.com".equals(email)) {
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter the admin password.", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyAdminFromFirestore(password);
        } else {
            // 2. If it's any other email, log in as a user
            loginAsUser(email);
        }
    }

    private void verifyAdminFromFirestore(String password) {
        db.collection("adminlogin").document("admin@vibestreams.com").get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && password.equals(documentSnapshot.getString("password"))) {
                    Toast.makeText(activity_login.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(activity_login.this, AdminPanelActivity.class));
                    finish();
                } else {
                    Toast.makeText(activity_login.this, "Invalid Admin Credentials.", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(activity_login.this, "Error: Could not connect to database.", Toast.LENGTH_SHORT).show());
    }

    private void loginAsUser(String userEmail) {
        // For any non-admin email, proceed directly to the user home page.
        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, activity_user_home.class);
        // Pass the user's email to the home activity so it knows who is logged in
        intent.putExtra("USER_EMAIL", userEmail);
        startActivity(intent);
        finish();
    }
}

package com.example.gamehub_m.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamehub_m.MainActivity;
import com.example.gamehub_m.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Login Activity - Handles user authentication with Firebase
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private ProgressBar loadingProgressBar;
    private TextView createAccountLink;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
            return;
        }

        // Initialize views
        initViews();
        setupListeners();
    }

    private void initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);
        createAccountLink = findViewById(R.id.createAccountLink);
    }

    private void setupListeners() {
        // Text change listeners for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };

        emailInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);

        // Login button click
        loginButton.setOnClickListener(v -> attemptLogin());

        // Create account link click
        createAccountLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Forgot password click
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> sendPasswordResetEmail());
        }
    }

    private void validateForm() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        boolean isValid = true;

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.setError(null);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.invalid_username));
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.setError(null);
        } else if (password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.invalid_password));
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        // Enable login button only if valid
        loginButton.setEnabled(isValid && !email.isEmpty() && !password.isEmpty());
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Show loading
        setLoading(true);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        String displayName = user != null && user.getDisplayName() != null 
                                ? user.getDisplayName() 
                                : email.split("@")[0];
                        Toast.makeText(this, getString(R.string.welcome) + displayName, Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        // Sign in failed
                        setLoading(false);
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : getString(R.string.login_failed);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        passwordInputLayout.setError(getString(R.string.login_failed));
                    }
                });
    }

    private void sendPasswordResetEmail() {
        String email = emailInput.getText().toString().trim();
        
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address first", Toast.LENGTH_SHORT).show();
            emailInput.requestFocus();
            return;
        }

        setLoading(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null 
                                ? task.getException().getMessage() 
                                : "Failed to send reset email";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Exit app instead of going back
        super.onBackPressed();
        finishAffinity();
    }
}
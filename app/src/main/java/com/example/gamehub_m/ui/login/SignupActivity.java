package com.example.gamehub_m.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamehub_m.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Signup Activity - Handles user registration with Firebase
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputLayout usernameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    
    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    
    private Button signupButton;
    private ProgressBar loadingProgressBar;
    private TextView signInLink;
    private ImageButton backButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initViews();
        setupListeners();
    }

    private void initViews() {
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        
        signupButton = findViewById(R.id.signupButton);
        loadingProgressBar = findViewById(R.id.loading);
        signInLink = findViewById(R.id.signInLink);
        backButton = findViewById(R.id.backButton);
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

        usernameInput.addTextChangedListener(textWatcher);
        emailInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);
        confirmPasswordInput.addTextChangedListener(textWatcher);

        // Signup button click
        signupButton.setOnClickListener(v -> attemptSignup());

        // Sign in link click
        signInLink.setOnClickListener(v -> finish());

        // Back button click
        backButton.setOnClickListener(v -> finish());
    }

    private void validateForm() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        boolean isValid = true;

        // Validate username
        if (username.isEmpty()) {
            usernameInputLayout.setError(null);
        } else if (username.length() < 3) {
            usernameInputLayout.setError(getString(R.string.invalid_username_signup));
            isValid = false;
        } else {
            usernameInputLayout.setError(null);
        }

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

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.setError(null);
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordInputLayout.setError(getString(R.string.passwords_not_match));
            isValid = false;
        } else {
            confirmPasswordInputLayout.setError(null);
        }

        // Enable signup button only if all fields are valid and not empty
        signupButton.setEnabled(isValid && 
            !username.isEmpty() && 
            !email.isEmpty() && 
            !password.isEmpty() && 
            !confirmPassword.isEmpty());
    }

    private void attemptSignup() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Show loading
        setLoading(true);

        // Create user with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful, update display name
                        if (mAuth.getCurrentUser() != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            mAuth.getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        // Sign out so user can log in with new account
                                        mAuth.signOut();
                                        setLoading(false);
                                        Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        } else {
                            setLoading(false);
                            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        // Registration failed
                        setLoading(false);
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : getString(R.string.registration_failed);
                        
                        // Check for specific Firebase errors
                        if (errorMessage.contains("email address is already in use")) {
                            emailInputLayout.setError(getString(R.string.email_exists));
                        }
                        
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        signupButton.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        usernameInput.setEnabled(!isLoading);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
        confirmPasswordInput.setEnabled(!isLoading);
    }
}

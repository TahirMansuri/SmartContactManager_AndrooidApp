package com.imrd.smartcontacts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.User;
import com.imrd.smartcontacts.util.PinHashUtil;
import com.imrd.smartcontacts.util.SessionManager;

/**
 * LoginActivity.java
 * -------------------------------------------------
 * Login screen. User enters username + PIN.
 * On success: creates session → goes to MainActivity.
 * -------------------------------------------------
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout   tilUsername, tilPin;
    private TextInputEditText etUsername, etPin;
    private Button            btnLogin;
    private TextView          tvGoRegister;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        // If already logged in → skip to main
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        bindViews();
        setupButtons();
    }

    private void bindViews() {
        tilUsername  = findViewById(R.id.til_username);
        tilPin       = findViewById(R.id.til_pin);
        etUsername   = findViewById(R.id.et_username);
        etPin        = findViewById(R.id.et_pin);
        btnLogin     = findViewById(R.id.btn_login);
        tvGoRegister = findViewById(R.id.tv_go_register);
    }

    private void setupButtons() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        clearErrors();

        String username = getText(etUsername).toLowerCase();
        String pin      = getText(etPin);
        boolean hasError = false;

        if (TextUtils.isEmpty(username)) { tilUsername.setError("Username is required"); hasError = true; }
        if (TextUtils.isEmpty(pin))      { tilPin     .setError("PIN is required");      hasError = true; }
        if (hasError) return;

        User user = dbHelper.getUserByUsername(username);
        if (user == null) {
            tilUsername.setError("Username not found"); return;
        }
        if (!PinHashUtil.verify(pin, user.getPinHash())) {
            tilPin.setError("Incorrect PIN"); return;
        }

        sessionManager.createSession(user.getId(), user.getFullName(), user.getUsername());
        Toast.makeText(this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
        goToMain();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilPin     .setError(null);
    }
}

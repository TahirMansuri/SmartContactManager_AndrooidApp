package com.imrd.smartcontacts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
 * RegisterActivity.java
 * -------------------------------------------------
 * New user registration screen.
 *
 * Fields:
 *   Full Name   – display name
 *   Username    – unique login id (no spaces)
 *   PIN         – 4–6 digits
 *   Confirm PIN – must match PIN
 *
 * On success: creates session → goes to MainActivity.
 * -------------------------------------------------
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout   tilFullName, tilUsername, tilPin, tilConfirmPin;
    private TextInputEditText etFullName, etUsername, etPin, etConfirmPin;
    private Button            btnRegister;
    private TextView          tvGoLogin;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        bindViews();
        setupButtons();
    }

    private void bindViews() {
        tilFullName   = findViewById(R.id.til_full_name);
        tilUsername   = findViewById(R.id.til_username);
        tilPin        = findViewById(R.id.til_pin);
        tilConfirmPin = findViewById(R.id.til_confirm_pin);
        etFullName    = findViewById(R.id.et_full_name);
        etUsername    = findViewById(R.id.et_username);
        etPin         = findViewById(R.id.et_pin);
        etConfirmPin  = findViewById(R.id.et_confirm_pin);
        btnRegister   = findViewById(R.id.btn_register);
        tvGoLogin     = findViewById(R.id.tv_go_login);
    }

    private void setupButtons() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        clearErrors();

        String fullName   = getText(etFullName);
        String username   = getText(etUsername).toLowerCase();
        String pin        = getText(etPin);
        String confirmPin = getText(etConfirmPin);

        boolean hasError = false;

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required"); hasError = true;
        }
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required"); hasError = true;
        } else if (username.contains(" ")) {
            tilUsername.setError("Username cannot contain spaces"); hasError = true;
        } else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters"); hasError = true;
        }
        if (TextUtils.isEmpty(pin)) {
            tilPin.setError("PIN is required"); hasError = true;
        } else if (!pin.matches("\\d{4,6}")) {
            tilPin.setError("PIN must be 4–6 digits"); hasError = true;
        }
        if (TextUtils.isEmpty(confirmPin)) {
            tilConfirmPin.setError("Please confirm your PIN"); hasError = true;
        } else if (!pin.equals(confirmPin)) {
            tilConfirmPin.setError("PINs do not match"); hasError = true;
        }

        if (hasError) return;

        if (dbHelper.isUsernameExists(username)) {
            tilUsername.setError("This username is already taken"); return;
        }

        String pinHash = PinHashUtil.hash(pin);
        User   user    = new User(fullName, username, pinHash);
        long   result  = dbHelper.registerUser(user);

        if (result > 0) {
            sessionManager.createSession((int) result, fullName, username);
            Toast.makeText(this, "Welcome, " + fullName + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity(); // clear back stack
        } else {
            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilFullName.setError(null); tilUsername.setError(null);
        tilPin     .setError(null); tilConfirmPin.setError(null);
    }
}

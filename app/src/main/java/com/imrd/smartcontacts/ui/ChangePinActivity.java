package com.imrd.smartcontacts.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.User;
import com.imrd.smartcontacts.util.PinHashUtil;
import com.imrd.smartcontacts.util.SessionManager;

/**
 * ChangePinActivity.java  — NEW FILE (Batch 2)
 * Allows a logged-in user to change their PIN.
 * Steps:
 *   1. Enter current PIN (verify against stored hash)
 *   2. Enter new PIN (4–6 digits)
 *   3. Confirm new PIN
 */
public class ChangePinActivity extends AppCompatActivity {

    private TextInputLayout   tilCurrentPin, tilNewPin, tilConfirmPin;
    private TextInputEditText etCurrentPin, etNewPin, etConfirmPin;
    private Button            btnChangePin;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        userId         = sessionManager.getLoggedInUserId();

        bindViews();
        setupToolbar();
        setupButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        tilCurrentPin = findViewById(R.id.til_current_pin);
        tilNewPin     = findViewById(R.id.til_new_pin);
        tilConfirmPin = findViewById(R.id.til_confirm_new_pin);
        etCurrentPin  = findViewById(R.id.et_current_pin);
        etNewPin      = findViewById(R.id.et_new_pin);
        etConfirmPin  = findViewById(R.id.et_confirm_new_pin);
        btnChangePin  = findViewById(R.id.btn_change_pin);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Change PIN");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupButton() {
        btnChangePin.setOnClickListener(v -> attemptChangePin());
    }

    private void attemptChangePin() {
        // Clear previous errors
        tilCurrentPin.setError(null);
        tilNewPin    .setError(null);
        tilConfirmPin.setError(null);

        String currentPin = getText(etCurrentPin);
        String newPin     = getText(etNewPin);
        String confirmPin = getText(etConfirmPin);

        boolean hasError = false;

        if (TextUtils.isEmpty(currentPin)) {
            tilCurrentPin.setError("Current PIN is required"); hasError = true;
        }
        if (TextUtils.isEmpty(newPin)) {
            tilNewPin.setError("New PIN is required"); hasError = true;
        } else if (!newPin.matches("\\d{4,6}")) {
            tilNewPin.setError("PIN must be 4–6 digits"); hasError = true;
        }
        if (TextUtils.isEmpty(confirmPin)) {
            tilConfirmPin.setError("Please confirm new PIN"); hasError = true;
        } else if (!newPin.equals(confirmPin)) {
            tilConfirmPin.setError("PINs do not match"); hasError = true;
        }
        if (hasError) return;

        // Verify current PIN
        User user = dbHelper.getUserById(userId);
        if (user == null) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PinHashUtil.verify(currentPin, user.getPinHash())) {
            tilCurrentPin.setError("Incorrect current PIN");
            return;
        }

        // Check new PIN is different from current
        if (currentPin.equals(newPin)) {
            tilNewPin.setError("New PIN must be different from current PIN");
            return;
        }

        // Update PIN in DB
        String newHash = PinHashUtil.hash(newPin);
        boolean success = dbHelper.updatePin(userId, newHash);
        if (success) {
            Toast.makeText(this, "PIN changed successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to change PIN. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}

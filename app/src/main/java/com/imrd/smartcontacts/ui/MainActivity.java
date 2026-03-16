package com.imrd.smartcontacts.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.adapter.ContactAdapter;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.BirthdayScheduler;
import com.imrd.smartcontacts.util.LocationHelper;
import com.imrd.smartcontacts.util.SessionManager;

import java.util.List;

/**
 * MainActivity.java  — MODIFIED (Batch 1)
 * Added: GPS Nearby toggle button, birthday alarm scheduling on login
 */
public class MainActivity extends AppCompatActivity {

    public static final int REQ_ADD_EDIT = 100;
    public static final int REQ_LOCATION = 301;

    private RecyclerView         recyclerView;
    private ContactAdapter       adapter;
    private EditText             etSearch;
    private View                 viewEmpty;
    private ImageButton          btnFilter, btnBackup, btnLogout, btnNearby;
    private FloatingActionButton fabAdd;
    private TextView             tvWelcome, tvNearbyLabel;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            userId;

    private boolean nearbyModeActive = false;
    private String  currentCity      = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
            return;
        }

        setContentView(R.layout.activity_main);
        dbHelper = DatabaseHelper.getInstance(this);
        userId   = sessionManager.getLoggedInUserId();

        bindViews();
        setupWelcome();
        setupRecyclerView();
        setupSearch();
        setupButtons();

        // Schedule daily birthday check at 8 AM
        BirthdayScheduler.scheduleDailyCheck(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn()) {
            if (nearbyModeActive && currentCity != null) loadNearbyContacts();
            else loadContacts("");
        }
    }

    private void bindViews() {
        recyclerView  = findViewById(R.id.recycler_contacts);
        etSearch      = findViewById(R.id.et_search);
        viewEmpty     = findViewById(R.id.tv_empty);
        btnFilter     = findViewById(R.id.btn_filter);
        btnBackup     = findViewById(R.id.btn_backup);
        btnLogout     = findViewById(R.id.btn_logout);
        btnNearby     = findViewById(R.id.btn_nearby);
        fabAdd        = findViewById(R.id.fab_add);
        tvWelcome     = findViewById(R.id.tv_welcome);
        tvNearbyLabel = findViewById(R.id.tv_nearby_label);
    }

    private void setupWelcome() {
        tvWelcome.setText("Hi, " + sessionManager.getLoggedInFullName() + " 👋");
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(this, null, contact -> {
            Intent intent = new Intent(this, ContactDetailActivity.class);
            intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_ID, contact.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!nearbyModeActive) loadContacts(s.toString().trim());
            }
        });
    }

    private void setupButtons() {
        fabAdd.setOnClickListener(v ->
            startActivityForResult(new Intent(this, AddEditContactActivity.class), REQ_ADD_EDIT));
        btnFilter.setOnClickListener(v ->
            startActivity(new Intent(this, FilterActivity.class)));
        btnBackup.setOnClickListener(v ->
            startActivity(new Intent(this, BackupRestoreActivity.class)));
        btnLogout.setOnClickListener(v -> confirmLogout());
        btnNearby.setOnClickListener(v -> toggleNearbyMode());
    }

    // ── GPS Nearby ────────────────────────────────────

    private void toggleNearbyMode() {
        if (nearbyModeActive) {
            nearbyModeActive = false;
            currentCity      = null;
            btnNearby.clearColorFilter();
            tvNearbyLabel.setVisibility(View.GONE);
            loadContacts("");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                             Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
            return;
        }
        detectCityAndFilter();
    }

    private void detectCityAndFilter() {
        Toast.makeText(this, "Detecting your location…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            String city = LocationHelper.getCurrentCity(this);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (city == null || city.isEmpty()) {
                    Toast.makeText(this, "Could not detect city. Check GPS is enabled.",
                        Toast.LENGTH_LONG).show();
                    return;
                }
                currentCity      = city;
                nearbyModeActive = true;
                btnNearby.setColorFilter(
                    getResources().getColor(R.color.colorAccent));
                tvNearbyLabel.setVisibility(View.VISIBLE);
                tvNearbyLabel.setText("📍 Showing contacts in: " + city);
                loadNearbyContacts();
            });
        }).start();
    }

    private void loadNearbyContacts() {
        List<Contact> contacts = dbHelper.filterByCity(currentCity, userId);
        adapter.updateList(contacts);
        recyclerView.setVisibility(contacts.isEmpty() ? View.GONE  : View.VISIBLE);
        viewEmpty   .setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectCityAndFilter();
        } else {
            Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (d, w) -> {
                sessionManager.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void loadContacts(String query) {
        List<Contact> contacts = query.isEmpty()
            ? dbHelper.getAllContacts(userId)
            : dbHelper.searchContacts(query, userId);
        adapter.updateList(contacts);
        recyclerView.setVisibility(contacts.isEmpty() ? View.GONE  : View.VISIBLE);
        viewEmpty   .setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

package com.imrd.smartcontacts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.adapter.ContactAdapter;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.SessionManager;

import java.util.List;

/**
 * MainActivity.java
 * Home screen — contact list, search, filter, logout, backup.
 * All contact queries are scoped to the logged-in user.
 */
public class MainActivity extends AppCompatActivity {

    public static final int REQ_ADD_EDIT = 100;

    // Views
    private RecyclerView         recyclerView;
    private ContactAdapter       adapter;
    private EditText             etSearch;
    private View                 viewEmpty;
    private ImageButton          btnFilter;
    private ImageButton          btnBackup;
    private ImageButton          btnLogout;
    private FloatingActionButton fabAdd;
    private TextView             tvWelcome;

    // State
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Security check — redirect to login if not logged in
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn()) loadContacts("");
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.recycler_contacts);
        etSearch     = findViewById(R.id.et_search);
        viewEmpty    = findViewById(R.id.tv_empty);
        btnFilter    = findViewById(R.id.btn_filter);
        btnBackup    = findViewById(R.id.btn_backup);
        btnLogout    = findViewById(R.id.btn_logout);
        fabAdd       = findViewById(R.id.fab_add);
        tvWelcome    = findViewById(R.id.tv_welcome);
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
                loadContacts(s.toString().trim());
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

        if (contacts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            viewEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            viewEmpty.setVisibility(View.GONE);
        }
    }
}

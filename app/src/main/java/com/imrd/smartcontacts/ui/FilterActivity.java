package com.imrd.smartcontacts.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.adapter.ContactAdapter;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    private static final String ALL_LABEL = "-- All --";

    private Spinner      spinnerCity, spinnerState;
    private Button       btnApply, btnReset;
    private RecyclerView recyclerView;
    private TextView     tvResultCount, tvEmpty;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ContactAdapter adapter;
    private int            userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        userId         = sessionManager.getLoggedInUserId();
        bindViews();
        setupToolbar();
        setupRecyclerView();
        populateSpinners();
        setupButtons();
        showContacts(dbHelper.getAllContacts(userId));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        spinnerCity   = findViewById(R.id.spinner_city);
        spinnerState  = findViewById(R.id.spinner_state);
        btnApply      = findViewById(R.id.btn_apply_filter);
        btnReset      = findViewById(R.id.btn_reset_filter);
        recyclerView  = findViewById(R.id.recycler_filtered);
        tvResultCount = findViewById(R.id.tv_result_count);
        tvEmpty       = findViewById(R.id.tv_filter_empty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Filter by Location");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(this, null, contact -> {});
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void populateSpinners() {
        List<String> cities = new ArrayList<>(); cities.add(ALL_LABEL); cities.addAll(dbHelper.getDistinctCities(userId));
        ArrayAdapter<String> ca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(ca);

        List<String> states = new ArrayList<>(); states.add(ALL_LABEL); states.addAll(dbHelper.getDistinctStates(userId));
        ArrayAdapter<String> sa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, states);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(sa);
    }

    private void setupButtons() {
        btnApply.setOnClickListener(v -> {
            String city  = spinnerCity .getSelectedItem().toString();
            String state = spinnerState.getSelectedItem().toString();
            showContacts(dbHelper.filterByLocation(
                city .equals(ALL_LABEL) ? "" : city,
                state.equals(ALL_LABEL) ? "" : state,
                userId));
        });
        btnReset.setOnClickListener(v -> { spinnerCity.setSelection(0); spinnerState.setSelection(0); showContacts(dbHelper.getAllContacts(userId)); });
    }

    private void showContacts(List<Contact> contacts) {
        adapter.updateList(contacts);
        int count = contacts.size();
        tvResultCount.setText(count + (count == 1 ? " contact found" : " contacts found"));
        recyclerView.setVisibility(contacts.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty     .setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

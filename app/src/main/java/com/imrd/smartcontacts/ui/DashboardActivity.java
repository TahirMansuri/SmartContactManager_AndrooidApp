package com.imrd.smartcontacts.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.CityStateData;
import com.imrd.smartcontacts.util.SessionManager;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardActivity.java  — NEW FILE (Batch 2)
 * Shows statistics about contacts:
 *   - Total contacts
 *   - Contacts per group
 *   - Contacts per state
 *   - Upcoming birthdays (next 7 days)
 *   - Recently added contacts (last 5)
 */
public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            userId;

    // Stat TextViews
    private TextView tvTotalContacts;
    private TextView tvWithPhoto;
    private TextView tvWithDob;
    private TextView tvUpcomingBirthdays;
    private TextView tvGroupBreakdown;
    private TextView tvStateBreakdown;
    private TextView tvRecentContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        userId         = sessionManager.getLoggedInUserId();

        bindViews();
        setupToolbar();
        loadStats();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        tvTotalContacts     = findViewById(R.id.tv_stat_total);
        tvWithPhoto         = findViewById(R.id.tv_stat_with_photo);
        tvWithDob           = findViewById(R.id.tv_stat_with_dob);
        tvUpcomingBirthdays = findViewById(R.id.tv_stat_upcoming_birthdays);
        tvGroupBreakdown    = findViewById(R.id.tv_stat_groups);
        tvStateBreakdown    = findViewById(R.id.tv_stat_states);
        tvRecentContacts    = findViewById(R.id.tv_stat_recent);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadStats() {
        List<Contact> all = dbHelper.getAllContacts(userId);
        int total = all.size();

        tvTotalContacts.setText(String.valueOf(total));

        if (total == 0) {
            tvWithPhoto        .setText("0");
            tvWithDob          .setText("0");
            tvUpcomingBirthdays.setText("None");
            tvGroupBreakdown   .setText("No contacts yet.");
            tvStateBreakdown   .setText("No contacts yet.");
            tvRecentContacts   .setText("No contacts yet.");
            return;
        }

        // Count with photo and DOB
        int withPhoto = 0, withDob = 0;
        for (Contact c : all) {
            if (c.hasPhoto()) withPhoto++;
            if (c.hasDob())   withDob++;
        }
        tvWithPhoto.setText(withPhoto + " of " + total);
        tvWithDob  .setText(withDob   + " of " + total);

        // Upcoming birthdays (next 7 days)
        StringBuilder upcomingBdays = new StringBuilder();
        Calendar today = Calendar.getInstance();
        int todayDay   = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH) + 1;
        int count = 0;
        for (Contact c : all) {
            if (!c.hasDob()) continue;
            try {
                String[] parts = c.getDob().split("/");
                int bDay   = Integer.parseInt(parts[0]);
                int bMonth = Integer.parseInt(parts[1]);
                // Check within next 7 days
                for (int d = 0; d <= 7; d++) {
                    Calendar check = Calendar.getInstance();
                    check.add(Calendar.DAY_OF_YEAR, d);
                    if (check.get(Calendar.DAY_OF_MONTH) == bDay &&
                        check.get(Calendar.MONTH) + 1    == bMonth) {
                        if (d == 0) upcomingBdays.append("🎂 Today: ");
                        else        upcomingBdays.append("🎂 In ").append(d).append("d: ");
                        upcomingBdays.append(c.getFullName()).append("\n");
                        count++;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
        tvUpcomingBirthdays.setText(count == 0 ? "None in next 7 days"
                                               : upcomingBdays.toString().trim());

        // Group breakdown
        Map<String, Integer> groupMap = new LinkedHashMap<>();
        for (String g : CityStateData.getGroups()) groupMap.put(g, 0);
        for (Contact c : all) {
            String g = (c.hasGroup()) ? c.getGroupTag() : "None";
            groupMap.put(g, groupMap.getOrDefault(g, 0) + 1);
        }
        StringBuilder groupSb = new StringBuilder();
        for (Map.Entry<String, Integer> e : groupMap.entrySet()) {
            if (e.getValue() > 0) groupSb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        tvGroupBreakdown.setText(groupSb.toString().trim());

        // State breakdown
        Map<String, Integer> stateMap = new LinkedHashMap<>();
        for (Contact c : all) {
            String s = c.getState();
            stateMap.put(s, stateMap.getOrDefault(s, 0) + 1);
        }
        StringBuilder stateSb = new StringBuilder();
        for (Map.Entry<String, Integer> e : stateMap.entrySet()) {
            stateSb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        tvStateBreakdown.setText(stateSb.toString().trim());

        // Recent contacts (last 5 by ID descending = most recently added)
        StringBuilder recentSb = new StringBuilder();
        int start = Math.max(0, all.size() - 5);
        for (int i = all.size() - 1; i >= start; i--) {
            recentSb.append("• ").append(all.get(i).getFullName()).append("\n");
        }
        tvRecentContacts.setText(recentSb.toString().trim());
    }
}

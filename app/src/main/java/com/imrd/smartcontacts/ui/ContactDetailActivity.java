package com.imrd.smartcontacts.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.ImageHelper;
import com.imrd.smartcontacts.util.SessionManager;

public class ContactDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT_ID = "contact_id";

    private CardView  cvDetailAvatar;
    private ImageView ivDetailPhoto;
    private TextView  tvDetailInitials, tvDetailName, tvDetailMobile,
                      tvDetailEmail, tvDetailCity, tvDetailState;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            contactId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        userId         = sessionManager.getLoggedInUserId();
        contactId      = getIntent().getIntExtra(EXTRA_CONTACT_ID, -1);
        if (contactId == -1) { finish(); return; }
        bindViews();
        setupToolbar();
        setupButtons();
    }

    @Override protected void onResume() { super.onResume(); loadContact(); }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        cvDetailAvatar   = findViewById(R.id.cv_detail_avatar);
        ivDetailPhoto    = findViewById(R.id.iv_detail_photo);
        tvDetailInitials = findViewById(R.id.tv_detail_initials);
        tvDetailName     = findViewById(R.id.tv_detail_name);
        tvDetailMobile   = findViewById(R.id.tv_detail_mobile);
        tvDetailEmail    = findViewById(R.id.tv_detail_email);
        tvDetailCity     = findViewById(R.id.tv_detail_city);
        tvDetailState    = findViewById(R.id.tv_detail_state);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Contact Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupButtons() {
        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);
        fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra(AddEditContactActivity.EXTRA_CONTACT_ID, contactId);
            startActivity(intent);
        });
        findViewById(R.id.btn_delete).setOnClickListener(v -> confirmDelete());
    }

    private void loadContact() {
        Contact c = dbHelper.getContactById(contactId, userId);
        if (c == null) { finish(); return; }
        if (c.hasPhoto()) {
            Bitmap bmp = ImageHelper.bytesToBitmap(c.getPhoto());
            if (bmp != null) {
                ivDetailPhoto.setImageBitmap(ImageHelper.toCircle(bmp));
                ivDetailPhoto.setVisibility(View.VISIBLE);
                tvDetailInitials.setVisibility(View.GONE);
                cvDetailAvatar.setCardBackgroundColor(0xFFEEEEEE);
            } else showInitials(c);
        } else showInitials(c);
        tvDetailName  .setText(c.getFullName());
        tvDetailMobile.setText(c.getMobile());
        tvDetailEmail .setText(c.getEmail());
        tvDetailCity  .setText(c.getCity());
        tvDetailState .setText(c.getState());
    }

    private void showInitials(Contact c) {
        ivDetailPhoto.setVisibility(View.GONE);
        tvDetailInitials.setVisibility(View.VISIBLE);
        tvDetailInitials.setText(c.getInitials());
        cvDetailAvatar.setCardBackgroundColor(0xFF1565C0);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")
            .setPositiveButton("Delete", (d, w) -> {
                dbHelper.deleteContact(contactId, userId);
                setResult(RESULT_OK);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}

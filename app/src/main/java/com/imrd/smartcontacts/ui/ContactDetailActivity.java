package com.imrd.smartcontacts.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.imrd.smartcontacts.util.VCardHelper;

/**
 * ContactDetailActivity.java  — MODIFIED (Batch 2)
 * Added: Share as vCard button
 * Kept: DOB, Group display, Quick actions (Call/WA/Email/SMS) from Batch 1
 */
public class ContactDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT_ID = "contact_id";

    private CardView     cvDetailAvatar;
    private ImageView    ivDetailPhoto;
    private TextView     tvDetailInitials, tvDetailName, tvDetailMobile,
                         tvDetailEmail, tvDetailCity, tvDetailState,
                         tvDetailDob, tvDetailGroup;
    private LinearLayout llDobRow, llGroupRow;
    private LinearLayout btnCall, btnWhatsApp, btnEmail, btnSms;
    private LinearLayout btnShare;  // NEW — share as vCard

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            contactId, userId;
    private Contact        currentContact;

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
        tvDetailDob      = findViewById(R.id.tv_detail_dob);
        tvDetailGroup    = findViewById(R.id.tv_detail_group);
        llDobRow         = findViewById(R.id.ll_dob_row);
        llGroupRow       = findViewById(R.id.ll_group_row);
        btnCall          = findViewById(R.id.btn_action_call);
        btnWhatsApp      = findViewById(R.id.btn_action_whatsapp);
        btnEmail         = findViewById(R.id.btn_action_email);
        btnSms           = findViewById(R.id.btn_action_sms);
        btnShare         = findViewById(R.id.btn_action_share);
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

        btnCall.setOnClickListener(v -> {
            if (currentContact == null) return;
            startActivity(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + currentContact.getMobile())));
        });

        btnWhatsApp.setOnClickListener(v -> {
            if (currentContact == null) return;
            String number = "91" + currentContact.getMobile();
            Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/" + number));
            intent.setPackage("com.whatsapp");
            if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
            else {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/" + number)));
                Toast.makeText(this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
            }
        });

        btnEmail.setOnClickListener(v -> {
            if (currentContact == null) return;
            Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse("mailto:" + currentContact.getEmail()));
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        btnSms.setOnClickListener(v -> {
            if (currentContact == null) return;
            startActivity(new Intent(Intent.ACTION_SENDTO,
                Uri.parse("smsto:" + currentContact.getMobile())));
        });

        // NEW — Share as vCard
        btnShare.setOnClickListener(v -> {
            if (currentContact == null) return;
            VCardHelper.shareContact(this, currentContact);
        });
    }

    private void loadContact() {
        currentContact = dbHelper.getContactById(contactId, userId);
        if (currentContact == null) { finish(); return; }

        if (currentContact.hasPhoto()) {
            Bitmap bmp = ImageHelper.bytesToBitmap(currentContact.getPhoto());
            if (bmp != null) {
                ivDetailPhoto.setImageBitmap(ImageHelper.toCircle(bmp));
                ivDetailPhoto.setVisibility(View.VISIBLE);
                tvDetailInitials.setVisibility(View.GONE);
                cvDetailAvatar.setCardBackgroundColor(0xFFEEEEEE);
            } else showInitials();
        } else showInitials();

        tvDetailName  .setText(currentContact.getFullName());
        tvDetailMobile.setText(currentContact.getMobile());
        tvDetailEmail .setText(currentContact.getEmail());
        tvDetailCity  .setText(currentContact.getCity());
        tvDetailState .setText(currentContact.getState());

        if (currentContact.hasDob()) {
            llDobRow.setVisibility(View.VISIBLE);
            int age = currentContact.getAge();
            String dobText = currentContact.getDob();
            if (age >= 0) dobText += "  (Age: " + age + ")";
            tvDetailDob.setText(dobText);
        } else {
            llDobRow.setVisibility(View.GONE);
        }

        if (currentContact.hasGroup()) {
            llGroupRow.setVisibility(View.VISIBLE);
            tvDetailGroup.setText(currentContact.getGroupTag());
            // also set in info card
            TextView tvGroupInfo = findViewById(R.id.tv_detail_group_info);
            if (tvGroupInfo != null) tvGroupInfo.setText(currentContact.getGroupTag());
        } else {
            llGroupRow.setVisibility(View.GONE);
        }
    }

    private void showInitials() {
        ivDetailPhoto.setVisibility(View.GONE);
        tvDetailInitials.setVisibility(View.VISIBLE);
        tvDetailInitials.setText(currentContact.getInitials());
        cvDetailAvatar.setCardBackgroundColor(0xFF1565C0);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete " + currentContact.getFullName() + "?")
            .setPositiveButton("Delete", (d, w) -> {
                dbHelper.deleteContact(contactId, userId);
                setResult(RESULT_OK); finish();
            })
            .setNegativeButton("Cancel", null).show();
    }
}

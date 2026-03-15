package com.imrd.smartcontacts.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.ImageHelper;
import com.imrd.smartcontacts.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditContactActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT_ID = "contact_id";

    private static final int REQ_CAMERA      = 101;
    private static final int REQ_GALLERY     = 102;
    private static final int REQ_CAMERA_PERM = 201;

    private CardView          cvAvatarContainer;
    private ImageView         ivPhoto;
    private TextView          tvInitialsOverlay;
    private TextInputLayout   tilFirstName, tilLastName, tilMobile, tilEmail, tilCity, tilState;
    private TextInputEditText etFirstName, etLastName, etMobile, etEmail, etCity, etState;
    private Button            btnSave;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            userId;
    private int            contactId          = -1;
    private boolean        isEditMode         = false;
    private byte[]         selectedPhotoBytes = null;
    private Uri            cameraImageUri     = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);
        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        userId         = sessionManager.getLoggedInUserId();
        bindViews();
        setupToolbar();
        checkMode();
        setupAvatarClick();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        cvAvatarContainer = findViewById(R.id.cv_avatar_container);
        ivPhoto           = findViewById(R.id.iv_photo);
        tvInitialsOverlay = findViewById(R.id.tv_initials_overlay);
        tilFirstName = findViewById(R.id.til_first_name); tilLastName = findViewById(R.id.til_last_name);
        tilMobile    = findViewById(R.id.til_mobile);     tilEmail    = findViewById(R.id.til_email);
        tilCity      = findViewById(R.id.til_city);       tilState    = findViewById(R.id.til_state);
        etFirstName  = findViewById(R.id.et_first_name);  etLastName  = findViewById(R.id.et_last_name);
        etMobile     = findViewById(R.id.et_mobile);      etEmail     = findViewById(R.id.et_email);
        etCity       = findViewById(R.id.et_city);        etState     = findViewById(R.id.et_state);
        btnSave      = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void checkMode() {
        if (getIntent().hasExtra(EXTRA_CONTACT_ID)) {
            isEditMode = true;
            contactId  = getIntent().getIntExtra(EXTRA_CONTACT_ID, -1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Contact");
            populateFields();
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Contact");
            showInitialsAvatar("?");
        }
        btnSave.setText(isEditMode ? "Update Contact" : "Save Contact");
        btnSave.setOnClickListener(v -> attemptSave());
    }

    private void populateFields() {
        Contact c = dbHelper.getContactById(contactId, userId);
        if (c == null) { finish(); return; }
        etFirstName.setText(c.getFirstName()); etLastName.setText(c.getLastName());
        etMobile.setText(c.getMobile());       etEmail.setText(c.getEmail());
        etCity.setText(c.getCity());           etState.setText(c.getState());
        selectedPhotoBytes = c.getPhoto();
        if (c.hasPhoto()) showPhotoAvatar(ImageHelper.bytesToBitmap(c.getPhoto()));
        else              showInitialsAvatar(c.getInitials());
    }

    private void setupAvatarClick() {
        cvAvatarContainer.setOnClickListener(v -> {
            PhotoPickerDialog dialog = new PhotoPickerDialog();
            dialog.setListener(new PhotoPickerDialog.PhotoSourceListener() {
                @Override public void onCameraSelected()  { openCamera();  }
                @Override public void onGallerySelected() { openGallery(); }
            });
            dialog.show(getSupportFragmentManager(), "photo_picker");
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERM);
            return;
        }
        launchCamera();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) { Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show(); return; }
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File photoFile = File.createTempFile("CONTACT_" + ts, ".jpg", getCacheDir());
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, REQ_CAMERA);
        } catch (IOException e) { Toast.makeText(this, "Could not start camera.", Toast.LENGTH_SHORT).show(); }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQ_GALLERY && data != null && data.getData() != null) applyPhoto(ImageHelper.uriToBytes(this, data.getData()));
        else if (requestCode == REQ_CAMERA && cameraImageUri != null) applyPhoto(ImageHelper.uriToBytes(this, cameraImageUri));
    }

    private void applyPhoto(byte[] bytes) {
        if (bytes == null) { Toast.makeText(this, "Could not load image.", Toast.LENGTH_SHORT).show(); return; }
        selectedPhotoBytes = bytes;
        showPhotoAvatar(ImageHelper.bytesToBitmap(bytes));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERM && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) launchCamera();
        else Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
    }

    private void showPhotoAvatar(Bitmap bitmap) {
        if (bitmap == null) return;
        ivPhoto.setImageBitmap(ImageHelper.toCircle(bitmap));
        ivPhoto.setVisibility(View.VISIBLE); tvInitialsOverlay.setVisibility(View.GONE);
        cvAvatarContainer.setCardBackgroundColor(0xFFEEEEEE);
    }

    private void showInitialsAvatar(String initials) {
        ivPhoto.setVisibility(View.GONE); tvInitialsOverlay.setVisibility(View.VISIBLE);
        tvInitialsOverlay.setText(initials == null || initials.isEmpty() ? "?" : initials);
        cvAvatarContainer.setCardBackgroundColor(0xFF1565C0);
    }

    private void attemptSave() {
        clearErrors();
        String firstName = getText(etFirstName), lastName = getText(etLastName),
               mobile = getText(etMobile), email = getText(etEmail),
               city = getText(etCity), state = getText(etState);
        boolean hasError = false;
        if (TextUtils.isEmpty(firstName)) { tilFirstName.setError("First name is required"); hasError = true; }
        if (TextUtils.isEmpty(lastName))  { tilLastName .setError("Last name is required");  hasError = true; }
        if (TextUtils.isEmpty(mobile))         { tilMobile.setError("Mobile number is required");            hasError = true; }
        else if (!mobile.matches("\\d{10}"))   { tilMobile.setError("Enter a valid 10-digit mobile number"); hasError = true; }
        if (TextUtils.isEmpty(email))          { tilEmail.setError("Email is required");                     hasError = true; }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.setError("Enter a valid email address"); hasError = true; }
        if (TextUtils.isEmpty(city))  { tilCity .setError("City is required");  hasError = true; }
        if (TextUtils.isEmpty(state)) { tilState.setError("State is required"); hasError = true; }
        if (hasError) return;

        int excludeId = isEditMode ? contactId : -1;
        if (dbHelper.isMobileExists(mobile, excludeId, userId)) { tilMobile.setError("Mobile already registered"); return; }
        if (dbHelper.isEmailExists (email,  excludeId, userId)) { tilEmail .setError("Email already registered");  return; }

        Contact contact = new Contact(firstName, lastName, mobile, email, city, state);
        contact.setPhoto(selectedPhotoBytes);
        if (isEditMode) {
            contact.setId(contactId);
            handleResult(dbHelper.updateContact(contact, userId), "Contact updated!", "Failed to update.");
        } else {
            handleResult((int) dbHelper.insertContact(contact, userId), "Contact saved!", "Failed to save.");
        }
    }

    private void handleResult(int result, String ok, String fail) {
        if (result > 0) { Toast.makeText(this, ok,   Toast.LENGTH_SHORT).show(); setResult(RESULT_OK); finish(); }
        else             { Toast.makeText(this, fail, Toast.LENGTH_SHORT).show(); }
    }

    private String getText(TextInputEditText et) { return et.getText() != null ? et.getText().toString().trim() : ""; }
    private void clearErrors() {
        tilFirstName.setError(null); tilLastName.setError(null); tilMobile.setError(null);
        tilEmail    .setError(null); tilCity    .setError(null); tilState .setError(null);
    }
}

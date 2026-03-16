package com.imrd.smartcontacts.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.BackupManager;
import com.imrd.smartcontacts.util.SessionManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BackupRestoreActivity.java  — MODIFIED (Batch 2)
 * Added: "Change PIN" button in a Security section at the top
 */
public class BackupRestoreActivity extends AppCompatActivity {

    private static final int REQ_PICK_FILE    = 301;
    private static final int REQ_STORAGE_PERM = 401;
    private static final int REQ_MANAGE_PERM  = 402;

    private TextView     tvBackupInfo;
    private Button       btnBackup, btnRestoreFromFile, btnChangePin;
    private LinearLayout llBackupFiles;
    private TextView     tvNoBackups;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int            userId;
    private String         username;

    private boolean pendingBackup  = false;
    private boolean pendingRestore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        userId         = sessionManager.getLoggedInUserId();
        username       = sessionManager.getLoggedInUsername();

        bindViews();
        setupToolbar();
        setupButtons();
        loadBackupFiles();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        tvBackupInfo       = findViewById(R.id.tv_backup_info);
        btnBackup          = findViewById(R.id.btn_backup);
        btnRestoreFromFile = findViewById(R.id.btn_restore_from_file);
        btnChangePin       = findViewById(R.id.btn_change_pin_shortcut);
        llBackupFiles      = findViewById(R.id.ll_backup_files);
        tvNoBackups        = findViewById(R.id.tv_no_backups);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Backup & Restore");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupButtons() {
        int count = dbHelper.getAllContacts(userId).size();
        tvBackupInfo.setText("You have " + count + " contact(s) that will be backed up.");

        btnBackup.setOnClickListener(v -> {
            pendingBackup = true;
            checkStoragePermissionAndProceed();
        });

        btnRestoreFromFile.setOnClickListener(v -> {
            pendingRestore = true;
            checkStoragePermissionAndProceed();
        });

        // Change PIN shortcut
        btnChangePin.setOnClickListener(v ->
            startActivity(new Intent(this, ChangePinActivity.class)));
    }

    private void checkStoragePermissionAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                proceedAfterPermission();
            } else {
                new AlertDialog.Builder(this)
                    .setTitle("Storage Permission Needed")
                    .setMessage("To save backup files to Downloads, please grant storage access in Settings.")
                    .setPositiveButton("Open Settings", (d, w) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQ_MANAGE_PERM);
                    })
                    .setNegativeButton("Cancel", (d, w) -> { pendingBackup = pendingRestore = false; })
                    .show();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission();
            } else {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                 Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_STORAGE_PERM);
            }
        }
    }

    private void proceedAfterPermission() {
        if (pendingBackup)  { pendingBackup  = false; doBackup(); }
        if (pendingRestore) { pendingRestore = false; pickFile(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE_PERM && grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            proceedAfterPermission();
        } else {
            Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
            pendingBackup = pendingRestore = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_MANAGE_PERM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                proceedAfterPermission();
            } else {
                Toast.makeText(this, "Storage permission not granted.", Toast.LENGTH_SHORT).show();
                pendingBackup = pendingRestore = false;
            }
            return;
        }
        if (requestCode == REQ_PICK_FILE && resultCode == Activity.RESULT_OK
            && data != null && data.getData() != null) {
            String path = resolveFilePath(data.getData());
            if (path != null) doRestore(path);
            else Toast.makeText(this, "Could not read selected file.", Toast.LENGTH_SHORT).show();
        }
    }

    private void doBackup() {
        List<Contact> contacts = dbHelper.getAllContactsForBackup(userId);
        if (contacts.isEmpty()) { Toast.makeText(this, "No contacts to back up.", Toast.LENGTH_SHORT).show(); return; }
        BackupManager.BackupResult result = BackupManager.backup(contacts, username);
        if (result.success) {
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            loadBackupFiles();
        } else {
            new AlertDialog.Builder(this).setTitle("Backup Failed")
                .setMessage(result.message).setPositiveButton("OK", null).show();
        }
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Backup File"), REQ_PICK_FILE);
    }

    private void doRestore(String filePath) {
        BackupManager.RestoreResult result = BackupManager.restore(filePath);
        if (!result.success) {
            new AlertDialog.Builder(this).setTitle("Restore Failed")
                .setMessage(result.message).setPositiveButton("OK", null).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Confirm Restore")
            .setMessage(result.message + "\n\nDuplicates will be skipped.\nProceed?")
            .setPositiveButton("Restore", (d, w) -> importContacts(result.contacts))
            .setNegativeButton("Cancel", null).show();
    }

    private void importContacts(List<Contact> contacts) {
        int imported = 0, skipped = 0;
        for (Contact c : contacts) {
            long res = dbHelper.insertContact(c, userId);
            if (res > 0) imported++; else skipped++;
        }
        String msg = imported + " contact(s) restored.";
        if (skipped > 0) msg += "\n" + skipped + " skipped (duplicates).";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        tvBackupInfo.setText("You have " + dbHelper.getAllContacts(userId).size() + " contact(s) that will be backed up.");
    }

    private void loadBackupFiles() {
        llBackupFiles.removeAllViews();
        List<File> files = BackupManager.getBackupFiles();
        if (files.isEmpty()) {
            tvNoBackups.setVisibility(View.VISIBLE);
            llBackupFiles.setVisibility(View.GONE);
            return;
        }
        tvNoBackups.setVisibility(View.GONE);
        llBackupFiles.setVisibility(View.VISIBLE);
        for (File file : files) {
            View row = getLayoutInflater().inflate(R.layout.item_backup_file, llBackupFiles, false);
            TextView tvName    = row.findViewById(R.id.tv_backup_file_name);
            TextView tvDate    = row.findViewById(R.id.tv_backup_file_date);
            Button   btnRestore = row.findViewById(R.id.btn_restore_this);
            tvName.setText(file.getName());
            tvDate.setText("Modified: " + new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(new Date(file.lastModified())));
            btnRestore.setOnClickListener(v -> doRestore(file.getAbsolutePath()));
            llBackupFiles.addView(row);
        }
    }

    private String resolveFilePath(Uri uri) {
        try {
            if ("file".equals(uri.getScheme())) return uri.getPath();
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            File tmp = new File(getCacheDir(), "restore_tmp.json");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
            byte[] buf = new byte[4096]; int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close(); is.close();
            return tmp.getAbsolutePath();
        } catch (Exception e) { e.printStackTrace(); return null; }
    }
}

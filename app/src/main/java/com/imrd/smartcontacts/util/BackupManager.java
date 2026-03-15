package com.imrd.smartcontacts.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;

import com.imrd.smartcontacts.model.Contact;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BackupManager.java
 * -------------------------------------------------
 * Handles JSON backup and restore of contacts.
 *
 * Backup format (contacts_backup_USERNAME_DATE.json):
 * {
 *   "version": 1,
 *   "username": "john",
 *   "exported_at": "2025-01-01 10:00",
 *   "contacts": [
 *     {
 *       "first_name": "Alice",
 *       "last_name": "Smith",
 *       "mobile": "9876543210",
 *       "email": "alice@example.com",
 *       "city": "Mumbai",
 *       "state": "Maharashtra",
 *       "photo": "<base64 string or null>"
 *     }, ...
 *   ]
 * }
 *
 * Saved to: Downloads/SmartContacts/
 * -------------------------------------------------
 */
public class BackupManager {

    private static final String BACKUP_FOLDER  = "SmartContacts";
    private static final int    BACKUP_VERSION = 1;

    public static class BackupResult {
        public final boolean success;
        public final String  message;
        public final String  filePath;

        BackupResult(boolean success, String message, String filePath) {
            this.success  = success;
            this.message  = message;
            this.filePath = filePath;
        }
    }

    public static class RestoreResult {
        public final boolean       success;
        public final String        message;
        public final List<Contact> contacts; // parsed contacts (photo included)

        RestoreResult(boolean success, String message, List<Contact> contacts) {
            this.success  = success;
            this.message  = message;
            this.contacts = contacts;
        }
    }

    // ── Backup ────────────────────────────────────

    /**
     * Serialises the contact list to JSON and writes it
     * to Downloads/SmartContacts/contacts_backup_USERNAME_DATE.json
     */
    public static BackupResult backup(List<Contact> contacts, String username) {
        try {
            // Build JSON
            JSONObject root = new JSONObject();
            root.put("version",     BACKUP_VERSION);
            root.put("username",    username);
            root.put("exported_at", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

            JSONArray array = new JSONArray();
            for (Contact c : contacts) {
                JSONObject obj = new JSONObject();
                obj.put("first_name", c.getFirstName());
                obj.put("last_name",  c.getLastName());
                obj.put("mobile",     c.getMobile());
                obj.put("email",      c.getEmail());
                obj.put("city",       c.getCity());
                obj.put("state",      c.getState());
                // Encode photo as Base64 string (or null)
                if (c.hasPhoto()) {
                    obj.put("photo", Base64.encodeToString(
                        c.getPhoto(), Base64.NO_WRAP));
                } else {
                    obj.put("photo", JSONObject.NULL);
                }
                array.put(obj);
            }
            root.put("contacts", array);

            // Write to Downloads/SmartContacts/
            File dir = getBackupDir();
            if (!dir.exists()) dir.mkdirs();

            String date     = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
            String fileName = "contacts_backup_" + username + "_" + date + ".json";
            File   file     = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(root.toString(2).getBytes("UTF-8"));
            fos.close();

            return new BackupResult(true,
                contacts.size() + " contacts backed up to:\n" + file.getAbsolutePath(),
                file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            return new BackupResult(false, "Backup failed: " + e.getMessage(), null);
        }
    }

    // ── Restore ───────────────────────────────────

    /**
     * Reads a JSON backup file and returns the parsed contacts.
     * Caller is responsible for inserting them into the DB.
     */
    public static RestoreResult restore(String filePath) {
        try {
            // Read file
            File file = new File(filePath);
            if (!file.exists())
                return new RestoreResult(false, "File not found: " + filePath, null);

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            // Parse JSON
            JSONObject root    = new JSONObject(sb.toString());
            JSONArray  array   = root.getJSONArray("contacts");
            List<Contact> list = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                byte[] photo = null;
                if (!obj.isNull("photo")) {
                    photo = Base64.decode(obj.getString("photo"), Base64.NO_WRAP);
                }
                Contact c = new Contact(
                    obj.getString("first_name"),
                    obj.getString("last_name"),
                    obj.getString("mobile"),
                    obj.getString("email"),
                    obj.getString("city"),
                    obj.getString("state")
                );
                c.setPhoto(photo);
                list.add(c);
            }

            return new RestoreResult(true,
                "Found " + list.size() + " contacts in backup.", list);

        } catch (Exception e) {
            e.printStackTrace();
            return new RestoreResult(false, "Restore failed: " + e.getMessage(), null);
        }
    }

    // ── List backup files ─────────────────────────

    /** Returns all .json backup files in the SmartContacts folder. */
    public static List<File> getBackupFiles() {
        List<File> files = new ArrayList<>();
        File dir = getBackupDir();
        if (dir.exists()) {
            File[] all = dir.listFiles(f -> f.getName().endsWith(".json"));
            if (all != null) {
                for (File f : all) files.add(f);
            }
        }
        return files;
    }

    // ── Helpers ───────────────────────────────────

    private static File getBackupDir() {
        return new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            BACKUP_FOLDER);
    }
}

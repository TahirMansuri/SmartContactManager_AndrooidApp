package com.imrd.smartcontacts.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper.java
 * -------------------------------------------------
 * Owns both SQLite tables:
 *
 * Table: users
 * ─────────────────────────────────────────────────
 * _id         INTEGER PRIMARY KEY AUTOINCREMENT
 * full_name   TEXT NOT NULL
 * username    TEXT NOT NULL UNIQUE
 * pin_hash    TEXT NOT NULL
 * created_at  INTEGER NOT NULL
 *
 * Table: contacts
 * ─────────────────────────────────────────────────
 * _id         INTEGER PRIMARY KEY AUTOINCREMENT
 * user_id     INTEGER NOT NULL  ← FK → users._id
 * first_name  TEXT NOT NULL
 * last_name   TEXT NOT NULL
 * mobile      TEXT NOT NULL
 * email       TEXT NOT NULL
 * city        TEXT NOT NULL
 * state       TEXT NOT NULL
 * photo       BLOB
 *
 * NOTE: mobile & email uniqueness is now scoped
 * per-user (same number can exist for different users).
 * -------------------------------------------------
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ── DB meta ──────────────────────────────────
    private static final String DB_NAME    = "smart_contacts.db";
    private static final int    DB_VERSION = 3; // v3: users table + user_id on contacts

    // ── Users table ───────────────────────────────
    public static final String TABLE_USERS    = "users";
    public static final String COL_U_ID       = "_id";
    public static final String COL_FULL_NAME  = "full_name";
    public static final String COL_USERNAME   = "username";
    public static final String COL_PIN_HASH   = "pin_hash";
    public static final String COL_CREATED_AT = "created_at";

    // ── Contacts table ────────────────────────────
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COL_ID         = "_id";
    public static final String COL_USER_ID    = "user_id";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME  = "last_name";
    public static final String COL_MOBILE     = "mobile";
    public static final String COL_EMAIL      = "email";
    public static final String COL_CITY       = "city";
    public static final String COL_STATE      = "state";
    public static final String COL_PHOTO      = "photo";

    // ── CREATE statements ─────────────────────────
    private static final String CREATE_USERS =
        "CREATE TABLE " + TABLE_USERS + " (" +
        COL_U_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_FULL_NAME  + " TEXT NOT NULL, " +
        COL_USERNAME   + " TEXT NOT NULL UNIQUE, " +
        COL_PIN_HASH   + " TEXT NOT NULL, " +
        COL_CREATED_AT + " INTEGER NOT NULL" +
        ");";

    private static final String CREATE_CONTACTS =
        "CREATE TABLE " + TABLE_CONTACTS + " (" +
        COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_USER_ID    + " INTEGER NOT NULL, " +
        COL_FIRST_NAME + " TEXT NOT NULL, " +
        COL_LAST_NAME  + " TEXT NOT NULL, " +
        COL_MOBILE     + " TEXT NOT NULL, " +
        COL_EMAIL      + " TEXT NOT NULL, " +
        COL_CITY       + " TEXT NOT NULL, " +
        COL_STATE      + " TEXT NOT NULL, " +
        COL_PHOTO      + " BLOB, " +
        "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_U_ID + ")" +
        ");";

    // ── Singleton ─────────────────────────────────
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null)
            instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ── Lifecycle ─────────────────────────────────

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COL_PHOTO + " BLOB");
        }
        if (oldVersion < 3) {
            // Create users table
            db.execSQL(CREATE_USERS);
            // Add user_id column to contacts (default 1 for existing rows)
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COL_USER_ID + " INTEGER NOT NULL DEFAULT 1");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ════════════════════════════════════════════════
    //  USER OPERATIONS
    // ════════════════════════════════════════════════

    /**
     * Register a new user.
     * @return positive row-id on success, -1 if username taken.
     */
    public long registerUser(User user) {
        if (isUsernameExists(user.getUsername())) return -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = new ContentValues();
        cv.put(COL_FULL_NAME,  user.getFullName());
        cv.put(COL_USERNAME,   user.getUsername());
        cv.put(COL_PIN_HASH,   user.getPinHash());
        cv.put(COL_CREATED_AT, user.getCreatedAt());
        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result;
    }

    /**
     * Fetch a user by username for login verification.
     * Returns null if username not found.
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
            COL_USERNAME + "=?", new String[]{username},
            null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                cursor.getInt   (cursor.getColumnIndexOrThrow(COL_U_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PIN_HASH)),
                cursor.getLong  (cursor.getColumnIndexOrThrow(COL_CREATED_AT))
            );
            cursor.close();
        }
        db.close();
        return user;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT " + COL_U_ID + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?",
            new String[]{username});
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    // ════════════════════════════════════════════════
    //  CONTACT OPERATIONS  (all scoped to userId)
    // ════════════════════════════════════════════════

    /**
     * Insert a new contact for the given user.
     * @return  positive id on success
     *          -1 if mobile duplicate for this user
     *          -2 if email  duplicate for this user
     *          -3 other error
     */
    public long insertContact(Contact c, int userId) {
        if (isMobileExists(c.getMobile(), -1, userId)) return -1;
        if (isEmailExists (c.getEmail(),  -1, userId)) return -2;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = toContentValues(c, userId);
        long result = db.insert(TABLE_CONTACTS, null, cv);
        db.close();
        return result > 0 ? result : -3;
    }

    /**
     * Update an existing contact (must belong to userId).
     * @return  1 on success, -1 mobile dup, -2 email dup, -3 error
     */
    public int updateContact(Contact c, int userId) {
        if (isMobileExists(c.getMobile(), c.getId(), userId)) return -1;
        if (isEmailExists (c.getEmail(),  c.getId(), userId)) return -2;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = toContentValues(c, userId);
        int rows = db.update(TABLE_CONTACTS, cv,
            COL_ID + "=? AND " + COL_USER_ID + "=?",
            new String[]{String.valueOf(c.getId()), String.valueOf(userId)});
        db.close();
        return rows > 0 ? 1 : -3;
    }

    public boolean deleteContact(int contactId, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_CONTACTS,
            COL_ID + "=? AND " + COL_USER_ID + "=?",
            new String[]{String.valueOf(contactId), String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    public Contact getContactById(int contactId, int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null,
            COL_ID + "=? AND " + COL_USER_ID + "=?",
            new String[]{String.valueOf(contactId), String.valueOf(userId)},
            null, null, null);
        Contact contact = null;
        if (cursor != null && cursor.moveToFirst()) {
            contact = cursorToContact(cursor);
            cursor.close();
        }
        db.close();
        return contact;
    }

    public List<Contact> getAllContacts(int userId) {
        return queryContacts(
            COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
            COL_FIRST_NAME + " ASC");
    }

    public List<Contact> searchContacts(String keyword, int userId) {
        String like = "%" + keyword + "%";
        String sel  = COL_USER_ID + "=? AND (" +
            COL_FIRST_NAME + " LIKE ? OR " + COL_LAST_NAME + " LIKE ? OR " +
            COL_MOBILE + " LIKE ? OR " + COL_EMAIL + " LIKE ?)";
        return queryContacts(sel,
            new String[]{String.valueOf(userId), like, like, like, like},
            COL_FIRST_NAME + " ASC");
    }

    public List<Contact> filterByLocation(String city, String state, int userId) {
        List<String> conditions = new ArrayList<>();
        List<String> args       = new ArrayList<>();
        conditions.add(COL_USER_ID + "=?");
        args.add(String.valueOf(userId));
        if (city  != null && !city.isEmpty())  { conditions.add(COL_CITY  + " LIKE ?"); args.add("%" + city  + "%"); }
        if (state != null && !state.isEmpty()) { conditions.add(COL_STATE + " LIKE ?"); args.add("%" + state + "%"); }
        String selection = String.join(" AND ", conditions);
        return queryContacts(selection, args.toArray(new String[0]),
            COL_STATE + " ASC, " + COL_CITY + " ASC, " + COL_FIRST_NAME + " ASC");
    }

    public List<String> getDistinctCities(int userId) {
        return getDistinctColumn(COL_CITY, userId);
    }

    public List<String> getDistinctStates(int userId) {
        return getDistinctColumn(COL_STATE, userId);
    }

    private List<String> getDistinctColumn(String column, int userId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT DISTINCT " + column + " FROM " + TABLE_CONTACTS +
            " WHERE " + COL_USER_ID + "=? ORDER BY " + column + " ASC",
            new String[]{String.valueOf(userId)});
        if (cursor != null) {
            while (cursor.moveToNext()) list.add(cursor.getString(0));
            cursor.close();
        }
        db.close();
        return list;
    }

    // ── Uniqueness checks (scoped per user) ────────

    public boolean isMobileExists(String mobile, int excludeContactId, int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT " + COL_ID + " FROM " + TABLE_CONTACTS +
            " WHERE " + COL_MOBILE + "=? AND " + COL_ID + "!=? AND " + COL_USER_ID + "=?",
            new String[]{mobile, String.valueOf(excludeContactId), String.valueOf(userId)});
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public boolean isEmailExists(String email, int excludeContactId, int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT " + COL_ID + " FROM " + TABLE_CONTACTS +
            " WHERE " + COL_EMAIL + "=? AND " + COL_ID + "!=? AND " + COL_USER_ID + "=?",
            new String[]{email, String.valueOf(excludeContactId), String.valueOf(userId)});
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    // ── Backup: get all contacts as list (no photo) ─

    /** Returns all contacts for a user — used for JSON backup. */
    public List<Contact> getAllContactsForBackup(int userId) {
        return getAllContacts(userId);
    }

    // ── Private helpers ────────────────────────────

    private List<Contact> queryContacts(String selection, String[] args, String orderBy) {
        List<Contact> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, selection, args, null, null, orderBy);
        if (cursor != null) {
            while (cursor.moveToNext()) list.add(cursorToContact(cursor));
            cursor.close();
        }
        db.close();
        return list;
    }

    private Contact cursorToContact(Cursor cursor) {
        int photoIndex = cursor.getColumnIndexOrThrow(COL_PHOTO);
        byte[] photo   = cursor.isNull(photoIndex) ? null : cursor.getBlob(photoIndex);
        return new Contact(
            cursor.getInt   (cursor.getColumnIndexOrThrow(COL_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRST_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_MOBILE)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_CITY)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_STATE)),
            photo
        );
    }

    private ContentValues toContentValues(Contact c, int userId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_ID,    userId);
        cv.put(COL_FIRST_NAME, c.getFirstName());
        cv.put(COL_LAST_NAME,  c.getLastName());
        cv.put(COL_MOBILE,     c.getMobile());
        cv.put(COL_EMAIL,      c.getEmail());
        cv.put(COL_CITY,       c.getCity());
        cv.put(COL_STATE,      c.getState());
        if (c.hasPhoto()) cv.put(COL_PHOTO, c.getPhoto());
        else              cv.putNull(COL_PHOTO);
        return cv;
    }
}

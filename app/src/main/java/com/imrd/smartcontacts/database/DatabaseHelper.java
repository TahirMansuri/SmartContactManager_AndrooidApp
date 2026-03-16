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
 * DatabaseHelper.java  — MODIFIED (Batch 1)
 * DB version bumped 3 → 4
 * New columns: dob TEXT, group_tag TEXT in contacts table
 * New methods: filterByCity(), filterByGroup(), updatePin(), getUserById()
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "smart_contacts.db";
    private static final int    DB_VERSION = 4;

    // Users table
    public static final String TABLE_USERS    = "users";
    public static final String COL_U_ID       = "_id";
    public static final String COL_FULL_NAME  = "full_name";
    public static final String COL_USERNAME   = "username";
    public static final String COL_PIN_HASH   = "pin_hash";
    public static final String COL_CREATED_AT = "created_at";

    // Contacts table
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
    public static final String COL_DOB        = "dob";
    public static final String COL_GROUP_TAG  = "group_tag";

    private static final String CREATE_USERS =
        "CREATE TABLE " + TABLE_USERS + "(" +
        COL_U_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        COL_FULL_NAME + " TEXT NOT NULL," +
        COL_USERNAME + " TEXT NOT NULL UNIQUE," +
        COL_PIN_HASH + " TEXT NOT NULL," +
        COL_CREATED_AT + " INTEGER NOT NULL);";

    private static final String CREATE_CONTACTS =
        "CREATE TABLE " + TABLE_CONTACTS + "(" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        COL_USER_ID + " INTEGER NOT NULL," +
        COL_FIRST_NAME + " TEXT NOT NULL," +
        COL_LAST_NAME + " TEXT NOT NULL," +
        COL_MOBILE + " TEXT NOT NULL," +
        COL_EMAIL + " TEXT NOT NULL," +
        COL_CITY + " TEXT NOT NULL," +
        COL_STATE + " TEXT NOT NULL," +
        COL_PHOTO + " BLOB," +
        COL_DOB + " TEXT," +
        COL_GROUP_TAG + " TEXT," +
        "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_U_ID + "));";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

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
            db.execSQL(CREATE_USERS);
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COL_USER_ID + " INTEGER NOT NULL DEFAULT 1");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COL_DOB + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COL_GROUP_TAG + " TEXT");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ══════════════════════════════════════════
    //  USER OPERATIONS
    // ══════════════════════════════════════════

    public long registerUser(User user) {
        if (isUsernameExists(user.getUsername())) return -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FULL_NAME,  user.getFullName());
        cv.put(COL_USERNAME,   user.getUsername());
        cv.put(COL_PIN_HASH,   user.getPinHash());
        cv.put(COL_CREATED_AT, user.getCreatedAt());
        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result;
    }

    public User getUserByUsername(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
            COL_USERNAME + "=?", new String[]{username}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_U_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PIN_HASH)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
            cursor.close();
        }
        db.close();
        return user;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
            COL_U_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_U_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PIN_HASH)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
            cursor.close();
        }
        db.close();
        return user;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_U_ID + " FROM " + TABLE_USERS +
            " WHERE " + COL_USERNAME + "=?", new String[]{username});
        boolean exists = c != null && c.getCount() > 0;
        if (c != null) c.close();
        db.close();
        return exists;
    }

    public boolean updatePin(int userId, String newPinHash) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PIN_HASH, newPinHash);
        int rows = db.update(TABLE_USERS, cv, COL_U_ID + "=?",
            new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // ══════════════════════════════════════════
    //  CONTACT OPERATIONS
    // ══════════════════════════════════════════

    public long insertContact(Contact c, int userId) {
        if (isMobileExists(c.getMobile(), -1, userId)) return -1;
        if (isEmailExists(c.getEmail(),   -1, userId)) return -2;
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(TABLE_CONTACTS, null, toContentValues(c, userId));
        db.close();
        return result > 0 ? result : -3;
    }

    public int updateContact(Contact c, int userId) {
        if (isMobileExists(c.getMobile(), c.getId(), userId)) return -1;
        if (isEmailExists(c.getEmail(),   c.getId(), userId)) return -2;
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.update(TABLE_CONTACTS, toContentValues(c, userId),
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
        return queryContacts(COL_USER_ID + "=?",
            new String[]{String.valueOf(userId)}, COL_FIRST_NAME + " ASC");
    }

    public List<Contact> searchContacts(String keyword, int userId) {
        String like = "%" + keyword + "%";
        String sel = COL_USER_ID + "=? AND (" +
            COL_FIRST_NAME + " LIKE ? OR " + COL_LAST_NAME + " LIKE ? OR " +
            COL_MOBILE + " LIKE ? OR " + COL_EMAIL + " LIKE ?)";
        return queryContacts(sel,
            new String[]{String.valueOf(userId), like, like, like, like},
            COL_FIRST_NAME + " ASC");
    }

    public List<Contact> filterByLocation(String city, String state, int userId) {
        List<String> conds = new ArrayList<>();
        List<String> args  = new ArrayList<>();
        conds.add(COL_USER_ID + "=?"); args.add(String.valueOf(userId));
        if (city  != null && !city.isEmpty())  { conds.add(COL_CITY  + " LIKE ?"); args.add("%" + city  + "%"); }
        if (state != null && !state.isEmpty()) { conds.add(COL_STATE + " LIKE ?"); args.add("%" + state + "%"); }
        return queryContacts(String.join(" AND ", conds), args.toArray(new String[0]),
            COL_STATE + " ASC, " + COL_CITY + " ASC, " + COL_FIRST_NAME + " ASC");
    }

    /** NEW — Filter by exact city name for GPS Nearby feature */
    public List<Contact> filterByCity(String city, int userId) {
        return queryContacts(
            COL_USER_ID + "=? AND " + COL_CITY + "=?",
            new String[]{String.valueOf(userId), city},
            COL_FIRST_NAME + " ASC");
    }

    /** NEW — Filter by group tag */
    public List<Contact> filterByGroup(String groupTag, int userId) {
        return queryContacts(
            COL_USER_ID + "=? AND " + COL_GROUP_TAG + "=?",
            new String[]{String.valueOf(userId), groupTag},
            COL_FIRST_NAME + " ASC");
    }

    public List<String> getDistinctCities(int userId)  { return getDistinctColumn(COL_CITY,  userId); }
    public List<String> getDistinctStates(int userId)  { return getDistinctColumn(COL_STATE, userId); }

    private List<String> getDistinctColumn(String col, int userId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT " + col + " FROM " + TABLE_CONTACTS +
            " WHERE " + COL_USER_ID + "=? ORDER BY " + col + " ASC",
            new String[]{String.valueOf(userId)});
        if (c != null) { while (c.moveToNext()) list.add(c.getString(0)); c.close(); }
        db.close();
        return list;
    }

    public boolean isMobileExists(String mobile, int excludeId, int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_ID + " FROM " + TABLE_CONTACTS +
            " WHERE " + COL_MOBILE + "=? AND " + COL_ID + "!=? AND " + COL_USER_ID + "=?",
            new String[]{mobile, String.valueOf(excludeId), String.valueOf(userId)});
        boolean exists = c != null && c.getCount() > 0;
        if (c != null) c.close();
        db.close();
        return exists;
    }

    public boolean isEmailExists(String email, int excludeId, int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_ID + " FROM " + TABLE_CONTACTS +
            " WHERE " + COL_EMAIL + "=? AND " + COL_ID + "!=? AND " + COL_USER_ID + "=?",
            new String[]{email, String.valueOf(excludeId), String.valueOf(userId)});
        boolean exists = c != null && c.getCount() > 0;
        if (c != null) c.close();
        db.close();
        return exists;
    }

    public List<Contact> getAllContactsForBackup(int userId) { return getAllContacts(userId); }

    // ── Private helpers ───────────────────────────

    private List<Contact> queryContacts(String sel, String[] args, String orderBy) {
        List<Contact> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, sel, args, null, null, orderBy);
        if (cursor != null) { while (cursor.moveToNext()) list.add(cursorToContact(cursor)); cursor.close(); }
        db.close();
        return list;
    }

    private Contact cursorToContact(Cursor cursor) {
        int photoIdx = cursor.getColumnIndexOrThrow(COL_PHOTO);
        byte[] photo = cursor.isNull(photoIdx) ? null : cursor.getBlob(photoIdx);
        int dobIdx   = cursor.getColumnIndex(COL_DOB);
        int grpIdx   = cursor.getColumnIndex(COL_GROUP_TAG);
        String dob   = (dobIdx >= 0 && !cursor.isNull(dobIdx)) ? cursor.getString(dobIdx) : null;
        String grp   = (grpIdx >= 0 && !cursor.isNull(grpIdx)) ? cursor.getString(grpIdx) : null;
        return new Contact(
            cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRST_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_MOBILE)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_CITY)),
            cursor.getString(cursor.getColumnIndexOrThrow(COL_STATE)),
            photo, dob, grp);
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
        if (c.hasPhoto()) cv.put(COL_PHOTO, c.getPhoto()); else cv.putNull(COL_PHOTO);
        if (c.hasDob())   cv.put(COL_DOB,   c.getDob());   else cv.putNull(COL_DOB);
        if (c.hasGroup()) cv.put(COL_GROUP_TAG, c.getGroupTag()); else cv.putNull(COL_GROUP_TAG);
        return cv;
    }
}

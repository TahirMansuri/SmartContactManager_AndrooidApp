package com.imrd.smartcontacts.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager.java
 * -------------------------------------------------
 * Manages the currently logged-in user using
 * SharedPreferences. Persists across app restarts
 * so the user stays logged in until they log out.
 *
 * Keys stored:
 *   is_logged_in  – boolean
 *   user_id       – int
 *   user_name     – String (full name for display)
 *   username      – String (login username)
 * -------------------------------------------------
 */
public class SessionManager {

    private static final String PREF_NAME     = "smart_contacts_session";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_FULL_NAME = "user_name";
    private static final String KEY_USERNAME  = "username";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /** Save login session after successful authentication. */
    public void createSession(int userId, String fullName, String username) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putInt    (KEY_USER_ID,   userId);
        editor.putString (KEY_FULL_NAME, fullName);
        editor.putString (KEY_USERNAME,  username);
        editor.apply();
    }

    /** Returns true if a user is currently logged in. */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    /** Returns the logged-in user's DB id, or -1 if not logged in. */
    public int getLoggedInUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /** Returns the logged-in user's full name. */
    public String getLoggedInFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    /** Returns the logged-in username. */
    public String getLoggedInUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    /** Clear session — called on logout. */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}

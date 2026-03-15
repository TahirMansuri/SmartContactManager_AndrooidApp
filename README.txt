# Smart Contact Intelligence System
### Android App | Java | SQLite | Classic Material Design

---

## PROJECT OVERVIEW

A fully functional Android contact management app with:
- Add / Edit / Delete contacts
- Unique mobile number & email validation (no duplicates)
- Real-time search by name, mobile, or email
- Location-based filtering by City and State
- Offline storage using SQLite (raw queries)

---

## PACKAGE STRUCTURE

com.imrd.smartcontacts
├── model/
│   └── Contact.java              ← Data class (POJO) for one contact
├── database/
│   └── DatabaseHelper.java       ← All SQLite CRUD operations
├── adapter/
│   └── ContactAdapter.java       ← RecyclerView adapter for contact list
└── ui/
    ├── MainActivity.java         ← Home: list + search
    ├── AddEditContactActivity.java  ← Add new / Edit existing contact
    ├── ContactDetailActivity.java   ← View full details + Delete
    └── FilterActivity.java          ← Filter contacts by City / State

---

## STEP-BY-STEP SETUP IN ANDROID STUDIO

### STEP 1 — Create a New Project

1. Open Android Studio
2. Click "New Project"
3. Select "Empty Views Activity"
4. Fill in:
   - Name:             SmartContactSystem
   - Package name:     com.imrd.smartcontacts
   - Save location:    (your choice)
   - Language:         Java
   - Minimum SDK:      API 24 (Android 7.0)
5. Click Finish and wait for Gradle sync

---

### STEP 2 — Replace build.gradle (app level)

Open  app/build.gradle  and replace everything with the provided
file content. Then click "Sync Now" in the yellow banner.

Dependencies added:
  - androidx.recyclerview:recyclerview:1.3.2
  - androidx.cardview:cardview:1.0.0
  - com.google.android.material:material:1.12.0

---

### STEP 3 — Create the Java Package Folders

In the Project panel (left sidebar):
  app > src > main > java > com.imrd.smartcontacts

Right-click on  com.imrd.smartcontacts  → New → Package:
  Create:  model
  Create:  database
  Create:  adapter
  Create:  ui

---

### STEP 4 — Create all Java files

Right-click each package → New → Java Class
Copy-paste the provided file contents exactly.

Files to create:
  model/      → Contact.java
  database/   → DatabaseHelper.java
  adapter/    → ContactAdapter.java
  ui/         → MainActivity.java
              → AddEditContactActivity.java
              → ContactDetailActivity.java
              → FilterActivity.java

---

### STEP 5 — Create Layout XML files

Navigate to:  app > src > main > res > layout

Android Studio creates activity_main.xml automatically.
For the rest, right-click layout folder → New → Layout Resource File.

Files to create (Type: LinearLayout or as provided):
  activity_main.xml             ← already exists, replace content
  activity_add_edit_contact.xml
  activity_contact_detail.xml
  activity_filter.xml
  item_contact.xml

---

### STEP 6 — Create Drawable XML files

Navigate to:  app > src > main > res > drawable

Right-click drawable → New → Drawable Resource File for each:

  bg_search.xml       ← rounded white background for search bar
  bg_spinner.xml      ← outlined box for spinners
  ic_add.xml          ← plus icon (FAB)
  ic_filter.xml       ← filter list icon
  ic_edit.xml         ← pencil icon (FAB in detail screen)
  ic_arrow_right.xml  ← chevron right (contact list rows)
  ic_delete.xml       ← trash icon
  ic_person.xml       ← person silhouette (empty state)

All vector icon files are provided — just copy-paste the XML.
You do NOT need to use Vector Asset Studio manually.

---

### STEP 7 — Replace res/values files

Replace these files with the provided content:
  res/values/colors.xml
  res/values/strings.xml
  res/values/themes.xml   (rename from themes/themes.xml if needed)

---

### STEP 8 — Replace AndroidManifest.xml

Open  app > src > main > AndroidManifest.xml
Replace the entire content with the provided file.

This registers all 4 activities:
  MainActivity              (LAUNCHER)
  AddEditContactActivity
  ContactDetailActivity
  FilterActivity

---

### STEP 9 — Run the App

1. Connect your Android device (USB debugging ON)
   OR use the API 36 emulator you already have
2. Click the green Run button (Shift+F10)
3. App will install and launch automatically

---

## DATABASE SCHEMA

Table: contacts

  Column      Type     Constraint
  ─────────── ──────── ──────────────────────────
  _id         INTEGER  PRIMARY KEY AUTOINCREMENT
  first_name  TEXT     NOT NULL
  last_name   TEXT     NOT NULL
  mobile      TEXT     NOT NULL UNIQUE
  email       TEXT     NOT NULL UNIQUE
  city        TEXT     NOT NULL
  state       TEXT     NOT NULL

Uniqueness is enforced at TWO levels:
  1. Java validation before DB call (shows user-friendly error)
  2. SQLite UNIQUE constraint (safety net)

---

## VALIDATION RULES

  Field         Rule
  ──────────    ──────────────────────────────────────────
  First Name    Required
  Last Name     Required
  Mobile        Required | exactly 10 digits | UNIQUE
  Email         Required | valid format | UNIQUE
  City          Required
  State         Required

---

## APP SCREENS & NAVIGATION

  MainActivity
    │
    ├── [FAB +]          → AddEditContactActivity  (ADD mode)
    ├── [Filter icon]    → FilterActivity
    └── [Contact row]    → ContactDetailActivity
                              │
                              ├── [FAB ✏️]  → AddEditContactActivity (EDIT mode)
                              └── [Delete]  → Confirmation dialog → back to MainActivity

---

## COMMON ERRORS & FIXES

ERROR: "Cannot resolve symbol 'R'"
FIX:   Build → Clean Project → Rebuild Project

ERROR: "Duplicate class androidx..."
FIX:   File → Invalidate Caches → Restart

ERROR: App crashes on launch
FIX:   Check Logcat for the red error line. Most likely cause is a
       missing drawable or a typo in AndroidManifest.xml activity name.

ERROR: "UNIQUE constraint failed"
FIX:   This should never reach the user — it's caught by isMobileExists()
       and isEmailExists() before the DB insert. If you see it in Logcat,
       the pre-check logic may have a package name mismatch.

---

## MODULE MAPPING (for your assignment)

  Assignment Module                 → Implementation
  ──────────────────────────────    ──────────────────────────────────
  User Interface Module             → All layout XML files + Toolbar
  Contact Management Module         → AddEditContactActivity + DatabaseHelper (insert/update/delete)
  Data Validation Module            → AddEditContactActivity.attemptSave() + DatabaseHelper.isMobileExists/isEmailExists
  Location-Based Filtering Module   → FilterActivity + DatabaseHelper.filterByLocation()
  Local Database Management Module  → DatabaseHelper.java (SQLiteOpenHelper)

---

Developed for:  TY BCA — Android Development Project
App Name:       Smart Contact Intelligence System
Language:       Java
Database:       SQLite (raw queries, no ORM)
UI Style:       Classic Material Design

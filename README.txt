# 📱 Smart Contact Intelligence System

![Android](https://img.shields.io/badge/Platform-Android-green)
![Java](https://img.shields.io/badge/Language-Java-orange)
![SQLite](https://img.shields.io/badge/Database-SQLite-blue)
![Min SDK](https://img.shields.io/badge/MinSDK-API24-yellow)
![Target SDK](https://img.shields.io/badge/TargetSDK-API35-brightgreen)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

> A feature-rich Android contact management application with multi-user authentication, location-based filtering, profile photos, and JSON backup/restore functionality.

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Modules](#modules)
- [Validation Rules](#validation-rules)
- [App Navigation](#app-navigation)
- [Common Errors & Fixes](#common-errors--fixes)
- [Developer](#developer)

---

## 📖 About

The **Smart Contact Intelligence System** is an Android-based mobile application designed to efficiently manage personal and professional contact information with enhanced validation, intelligent filtering, and multi-user security mechanisms.

Unlike traditional phonebook applications that store only basic contact details, this system incorporates structured attributes such as city, state, email address, and profile photo to enable advanced data organization and retrieval. The application enforces strict data integrity rules including unique email validation and duplicate contact number detection, thereby preventing redundant or inconsistent records.

Location-based indexing allows users to filter and sort contacts based on geographic attributes, which is especially useful for organizational, business, and regional communication requirements. The system also supports secure multi-user login with PIN authentication and JSON-based contact backup and restore functionality.

---

## ✨ Features

### 🔐 Authentication & Security
- Multi-user support — each user has a fully isolated contact list
- User Registration with Full Name, Username, and 4–6 digit PIN
- SHA-256 hashed PIN — raw PIN is never stored in the database
- Persistent login session using SharedPreferences
- Logout with confirmation dialog

### 👤 Contact Management
- Add, Edit, and Delete contacts
- Fields: First Name, Last Name, Mobile (10 digits), Email, City, State
- Profile photo from **Camera** or **Gallery** (circle cropped, WhatsApp style)
- Colored initials avatar shown when no photo is set
- Contacts sorted A–Z by first name

### 🔍 Search & Filter
- Real-time search by name, mobile number, or email
- Location-based filtering by City and/or State using dropdown spinners
- Contacts sorted by state → city → name when filtered

### ✅ Data Validation
- 10-digit mobile number format enforcement
- Valid email address format check
- Duplicate mobile number detection (scoped per user)
- Duplicate email detection (scoped per user)
- Validation at two levels: Java layer + SQLite constraints

### 💾 Backup & Restore
- Export all contacts to `Downloads/SmartContacts/` as a `.json` file
- Profile photos included in backup (Base64 encoded)
- Restore contacts from any `.json` backup file
- Duplicate contacts automatically skipped during restore
- View and restore from previously saved backup files

---

## 🛠 Tech Stack

| Category         | Technology                          |
|------------------|-------------------------------------|
| Language         | Java                                |
| Platform         | Android (API 24+)                   |
| Database         | SQLite (Raw Queries, no ORM)        |
| UI Style         | Classic Material Design             |
| Image Handling   | Android Bitmap API                  |
| Security         | SHA-256 PIN Hashing                 |
| Session          | SharedPreferences                   |
| Backup Format    | JSON + Base64                       |
| Build System     | Gradle                              |
| Min SDK          | API 24 (Android 7.0 Nougat)         |
| Target SDK       | API 35 (Android 15)                 |
| IDE              | Android Studio Meerkat 2024.3.2     |

> ✅ No third-party libraries used — pure Android SDK only.

---

## 📁 Project Structure

```
app/src/main/java/com/imrd/smartcontacts/
│
├── model/
│   ├── Contact.java                  ← Contact data class (POJO)
│   └── User.java                     ← User data class (POJO)
│
├── database/
│   └── DatabaseHelper.java           ← All SQLite CRUD — users + contacts
│
├── adapter/
│   └── ContactAdapter.java           ← RecyclerView adapter for contact list
│
├── util/
│   ├── ImageHelper.java              ← Bitmap compress, decode, circle crop
│   ├── BackupManager.java            ← JSON backup and restore logic
│   ├── SessionManager.java           ← SharedPreferences login session
│   └── PinHashUtil.java              ← SHA-256 PIN hashing utility
│
└── ui/
    ├── LoginActivity.java            ← Login screen
    ├── RegisterActivity.java         ← Registration screen
    ├── MainActivity.java             ← Home: contact list + search
    ├── AddEditContactActivity.java   ← Add / Edit contact form
    ├── ContactDetailActivity.java    ← Contact detail view + delete
    ├── FilterActivity.java           ← Filter by city / state
    ├── BackupRestoreActivity.java    ← Backup & restore screen
    └── PhotoPickerDialog.java        ← Camera / Gallery bottom sheet
```

---

## 🗄 Database Schema

### Table: `users`
| Column       | Type    | Constraint                    |
|--------------|---------|-------------------------------|
| `_id`        | INTEGER | PRIMARY KEY AUTOINCREMENT     |
| `full_name`  | TEXT    | NOT NULL                      |
| `username`   | TEXT    | NOT NULL UNIQUE               |
| `pin_hash`   | TEXT    | NOT NULL (SHA-256 hash)       |
| `created_at` | INTEGER | NOT NULL (Unix timestamp ms)  |

### Table: `contacts`
| Column       | Type    | Constraint                        |
|--------------|---------|-----------------------------------|
| `_id`        | INTEGER | PRIMARY KEY AUTOINCREMENT         |
| `user_id`    | INTEGER | NOT NULL, FOREIGN KEY → users._id |
| `first_name` | TEXT    | NOT NULL                          |
| `last_name`  | TEXT    | NOT NULL                          |
| `mobile`     | TEXT    | NOT NULL                          |
| `email`      | TEXT    | NOT NULL                          |
| `city`       | TEXT    | NOT NULL                          |
| `state`      | TEXT    | NOT NULL                          |
| `photo`      | BLOB    | nullable                          |

> Mobile and Email uniqueness is enforced **per user** — the same number or email can exist across different user accounts.

---

## 🧩 Modules

| Module                        | Description                                              | Key Files |
|-------------------------------|----------------------------------------------------------|-----------|
| **User Interface Module**     | All screens, layouts, navigation                         | `res/layout/*.xml`, all Activities |
| **Contact Management Module** | Add, Edit, Delete contacts                               | `AddEditContactActivity`, `DatabaseHelper` |
| **Data Validation Module**    | Unique mobile/email checks, format validation            | `AddEditContactActivity.attemptSave()`, `DatabaseHelper` |
| **Location Filtering Module** | Filter and sort contacts by city and state               | `FilterActivity`, `DatabaseHelper.filterByLocation()` |
| **Local Database Module**     | SQLite raw queries, schema, migrations                   | `DatabaseHelper` (SQLiteOpenHelper) |
| **Authentication Module**     | Register, Login, Session, PIN hashing                    | `LoginActivity`, `RegisterActivity`, `SessionManager`, `PinHashUtil` |
| **Backup & Restore Module**   | JSON export to Downloads, import from file               | `BackupManager`, `BackupRestoreActivity` |
| **Photo Management Module**   | Camera, Gallery, circle crop, BLOB storage               | `ImageHelper`, `PhotoPickerDialog` |

---

## ✔️ Validation Rules

| Field      | Rule                                               |
|------------|----------------------------------------------------|
| First Name | Required                                           |
| Last Name  | Required                                           |
| Mobile     | Required · exactly 10 digits · unique per user     |
| Email      | Required · valid format · unique per user          |
| City       | Required                                           |
| State      | Required                                           |
| Username   | Required · min 3 chars · no spaces · unique        |
| PIN        | Required · 4–6 digits only                         |

---

## 📱 App Navigation

```
App Launch
    └── LoginActivity  (LAUNCHER)
          ├── Session exists? → MainActivity (auto-login)
          └── No session
                ├── Login    → username + PIN → MainActivity
                └── Register → create account → MainActivity
                                    │
                        ┌───────────┴─────────────────┐
                        │         MainActivity         │
                        │   (contact list + search)    │
                        └───┬──────┬──────┬────────────┘
                            │      │      │         │
                         [FAB+] [Filter] [Backup] [Logout]
                            │      │      │         │
                        AddEdit  Filter  Backup   Login
                        Activity Activity Restore  Activity
                            │
                       ContactDetail
                         Activity
                            │
                      ┌─────┴──────┐
                    [Edit]       [Delete]
                      │
                   AddEdit
                   Activity
```

---

## 🐛 Common Errors & Fixes

| Error | Fix |
|-------|-----|
| `Cannot resolve symbol 'R'` | Build → Clean Project → Rebuild Project |
| `Duplicate class androidx...` | File → Invalidate Caches → Restart |
| `resource mipmap/ic_launcher not found` | Add `ic_launcher.xml` in `res/mipmap-anydpi-v26/` |
| `navigation package does not exist` | Delete `FirstFragment.java`, `SecondFragment.java`, `res/navigation/` |
| `AppBarConfiguration not found` | Replace auto-generated `MainActivity.java` with provided version |
| App crashes on launch | Check Logcat. Usually missing drawable or wrong activity name in Manifest |
| `UNIQUE constraint failed` | Caught by `isMobileExists()` / `isEmailExists()` pre-checks |
| Backup fails on Android 11+ | Grant `MANAGE_EXTERNAL_STORAGE` in Settings → Apps → Permissions |

---

## 👨‍💻 Developer

**Asst. Prof. Tahir Husen Najir Mansuri**

📧 [tahirmansuri086@gmail.com](mailto:tahirmansuri086@gmail.com)

---

<p align="center">Made with ❤️ using Java & Android SDK</p>
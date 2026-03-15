package com.imrd.smartcontacts.model;

/**
 * User.java
 * -------------------------------------------------
 * POJO representing one row in the SQLite "users" table.
 *
 * Fields
 * -------
 * id          – auto-generated primary key
 * fullName    – display name shown after login
 * username    – unique login identifier
 * pinHash     – SHA-256 hash of the PIN (never store raw PIN)
 * createdAt   – timestamp (ms) when account was created
 * -------------------------------------------------
 */
public class User {

    private int    id;
    private String fullName;
    private String username;
    private String pinHash;
    private long   createdAt;

    // ── Constructors ──────────────────────────────

    /** For inserting a new user (no id yet). */
    public User(String fullName, String username, String pinHash) {
        this.fullName  = fullName;
        this.username  = username;
        this.pinHash   = pinHash;
        this.createdAt = System.currentTimeMillis();
    }

    /** For reading back from DB. */
    public User(int id, String fullName, String username,
                String pinHash, long createdAt) {
        this.id        = id;
        this.fullName  = fullName;
        this.username  = username;
        this.pinHash   = pinHash;
        this.createdAt = createdAt;
    }

    // ── Getters ───────────────────────────────────

    public int    getId()        { return id;        }
    public String getFullName()  { return fullName;  }
    public String getUsername()  { return username;  }
    public String getPinHash()   { return pinHash;   }
    public long   getCreatedAt() { return createdAt; }

    // ── Setters ───────────────────────────────────

    public void setId(int id)              { this.id       = id;       }
    public void setFullName(String v)      { this.fullName = v;        }
    public void setUsername(String v)      { this.username = v;        }
    public void setPinHash(String v)       { this.pinHash  = v;        }
    public void setCreatedAt(long v)       { this.createdAt = v;       }
}

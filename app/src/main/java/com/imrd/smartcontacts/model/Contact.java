package com.imrd.smartcontacts.model;

/**
 * Contact.java
 * -------------------------------------------------
 * POJO representing one row in the SQLite "contacts"
 * table. Includes an optional photo field stored
 * as a byte array (BLOB) in the database.
 *
 * Fields
 * -------
 * id          – auto-generated primary key
 * firstName   – first name  (required)
 * lastName    – last name   (required)
 * mobile      – 10-digit mobile number (unique, required)
 * email       – email address (unique, required)
 * city        – city  name   (required)
 * state       – state name   (required)
 * photo       – profile picture as byte[] (optional, nullable)
 * -------------------------------------------------
 */
public class Contact {

    private int    id;
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String city;
    private String state;
    private byte[] photo;       // nullable – null means "no photo set"

    // ── Constructors ────────────────────────────────

    /** Used when creating a brand-new contact (no DB id yet, no photo). */
    public Contact(String firstName, String lastName,
                   String mobile,    String email,
                   String city,      String state) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.mobile    = mobile;
        this.email     = email;
        this.city      = city;
        this.state     = state;
        this.photo     = null;
    }

    /** Used when reading a contact back from the database (with photo). */
    public Contact(int id,
                   String firstName, String lastName,
                   String mobile,    String email,
                   String city,      String state,
                   byte[] photo) {
        this.id        = id;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.mobile    = mobile;
        this.email     = email;
        this.city      = city;
        this.state     = state;
        this.photo     = photo;
    }

    // ── Getters ─────────────────────────────────────

    public int    getId()        { return id;        }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName;  }
    public String getMobile()    { return mobile;    }
    public String getEmail()     { return email;     }
    public String getCity()      { return city;      }
    public String getState()     { return state;     }
    public byte[] getPhoto()     { return photo;     }

    /** Returns true if this contact has a profile photo saved. */
    public boolean hasPhoto()    { return photo != null && photo.length > 0; }

    // ── Setters ─────────────────────────────────────

    public void setId(int id)           { this.id        = id;        }
    public void setFirstName(String v)  { this.firstName = v;         }
    public void setLastName(String v)   { this.lastName  = v;         }
    public void setMobile(String v)     { this.mobile    = v;         }
    public void setEmail(String v)      { this.email     = v;         }
    public void setCity(String v)       { this.city      = v;         }
    public void setState(String v)      { this.state     = v;         }
    public void setPhoto(byte[] photo)  { this.photo     = photo;     }

    // ── Helpers ─────────────────────────────────────

    /** Convenience: full name shown in lists. */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Initials for the avatar circle (e.g. "AB" from "Alice Bob"). */
    public String getInitials() {
        String f = (firstName != null && !firstName.isEmpty())
                   ? String.valueOf(firstName.charAt(0)).toUpperCase() : "";
        String l = (lastName  != null && !lastName.isEmpty())
                   ? String.valueOf(lastName.charAt(0)).toUpperCase()  : "";
        return f + l;
    }
}

package com.imrd.smartcontacts.model;

/**
 * Contact.java  — MODIFIED (Batch 1)
 * Added: dob (Date of Birth), groupTag (Group/Tag label), getAge(), hasDob(), hasGroup()
 */
public class Contact {

    private int    id;
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String city;
    private String state;
    private byte[] photo;
    private String dob;        // format: dd/MM/yyyy  (nullable)
    private String groupTag;   // "Family", "Work", etc. (nullable)

    // ── Constructors ──────────────────────────────

    /** New contact — no id, no photo, no dob, no group. */
    public Contact(String firstName, String lastName,
                   String mobile,    String email,
                   String city,      String state) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.mobile    = mobile;
        this.email     = email;
        this.city      = city;
        this.state     = state;
    }

    /** Full constructor — reading from DB. */
    public Contact(int id,
                   String firstName, String lastName,
                   String mobile,    String email,
                   String city,      String state,
                   byte[] photo,     String dob,
                   String groupTag) {
        this.id        = id;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.mobile    = mobile;
        this.email     = email;
        this.city      = city;
        this.state     = state;
        this.photo     = photo;
        this.dob       = dob;
        this.groupTag  = groupTag;
    }

    // ── Getters ───────────────────────────────────

    public int    getId()        { return id;        }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName;  }
    public String getMobile()    { return mobile;    }
    public String getEmail()     { return email;     }
    public String getCity()      { return city;      }
    public String getState()     { return state;     }
    public byte[] getPhoto()     { return photo;     }
    public String getDob()       { return dob;       }
    public String getGroupTag()  { return groupTag;  }

    public boolean hasPhoto()  { return photo != null && photo.length > 0; }
    public boolean hasDob()    { return dob != null && !dob.isEmpty(); }
    public boolean hasGroup()  { return groupTag != null && !groupTag.isEmpty() && !groupTag.equals("None"); }

    // ── Setters ───────────────────────────────────

    public void setId(int id)            { this.id        = id;      }
    public void setFirstName(String v)   { this.firstName = v;       }
    public void setLastName(String v)    { this.lastName  = v;       }
    public void setMobile(String v)      { this.mobile    = v;       }
    public void setEmail(String v)       { this.email     = v;       }
    public void setCity(String v)        { this.city      = v;       }
    public void setState(String v)       { this.state     = v;       }
    public void setPhoto(byte[] photo)   { this.photo     = photo;   }
    public void setDob(String dob)       { this.dob       = dob;     }
    public void setGroupTag(String g)    { this.groupTag  = g;       }

    // ── Helpers ───────────────────────────────────

    public String getFullName() { return firstName + " " + lastName; }

    public String getInitials() {
        String f = (firstName != null && !firstName.isEmpty())
                   ? String.valueOf(firstName.charAt(0)).toUpperCase() : "";
        String l = (lastName  != null && !lastName.isEmpty())
                   ? String.valueOf(lastName.charAt(0)).toUpperCase()  : "";
        return f + l;
    }

    /** Returns age from DOB, or -1 if DOB not set. */
    public int getAge() {
        if (!hasDob()) return -1;
        try {
            String[] parts = dob.split("/");
            int dobYear  = Integer.parseInt(parts[2]);
            int dobMonth = Integer.parseInt(parts[1]);
            int dobDay   = Integer.parseInt(parts[0]);
            java.util.Calendar today = java.util.Calendar.getInstance();
            int age = today.get(java.util.Calendar.YEAR) - dobYear;
            if (today.get(java.util.Calendar.MONTH) + 1 < dobMonth ||
               (today.get(java.util.Calendar.MONTH) + 1 == dobMonth &&
                today.get(java.util.Calendar.DAY_OF_MONTH) < dobDay)) {
                age--;
            }
            return age;
        } catch (Exception e) { return -1; }
    }
}

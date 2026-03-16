package com.imrd.smartcontacts.util;

import com.imrd.smartcontacts.model.Contact;

import java.util.Comparator;
import java.util.List;

/**
 * SortHelper.java  — NEW FILE (Batch 2)
 * Provides sort options for the contact list.
 */
public class SortHelper {

    public static final int SORT_NAME_AZ   = 0;
    public static final int SORT_NAME_ZA   = 1;
    public static final int SORT_CITY      = 2;
    public static final int SORT_STATE     = 3;
    public static final int SORT_GROUP     = 4;

    public static final String[] SORT_LABELS = {
        "Name A → Z",
        "Name Z → A",
        "City",
        "State",
        "Group"
    };

    public static void sort(List<Contact> list, int sortOption) {
        switch (sortOption) {
            case SORT_NAME_AZ:
                list.sort((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()));
                break;
            case SORT_NAME_ZA:
                list.sort((a, b) -> b.getFullName().compareToIgnoreCase(a.getFullName()));
                break;
            case SORT_CITY:
                list.sort((a, b) -> a.getCity().compareToIgnoreCase(b.getCity()));
                break;
            case SORT_STATE:
                list.sort((a, b) -> a.getState().compareToIgnoreCase(b.getState()));
                break;
            case SORT_GROUP:
                list.sort((a, b) -> {
                    String ga = a.hasGroup() ? a.getGroupTag() : "zzz";
                    String gb = b.hasGroup() ? b.getGroupTag() : "zzz";
                    return ga.compareToIgnoreCase(gb);
                });
                break;
        }
    }
}

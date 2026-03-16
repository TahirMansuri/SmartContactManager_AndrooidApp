package com.imrd.smartcontacts.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CityStateData.java  — NEW FILE (Batch 1)
 * Hardcoded Indian states and cities for cascading spinners.
 * Also provides contact group/tag options.
 */
public class CityStateData {

    private static final Map<String, List<String>> STATE_CITY_MAP;

    static {
        STATE_CITY_MAP = new LinkedHashMap<>();

        STATE_CITY_MAP.put("Maharashtra", Arrays.asList(
            "Shahada", "Nandurbar", "Taloda", "Dondaicha",
            "Mumbai", "Pune", "Nashik", "Nagpur", "Aurangabad"
        ));

        STATE_CITY_MAP.put("Madhya Pradesh", Arrays.asList(
            "Khetiya", "Pansemal", "Badwani",
            "Indore", "Bhopal", "Jabalpur", "Gwalior"
        ));

        STATE_CITY_MAP.put("Gujarat", Arrays.asList(
            "Surat", "Baroda", "Ahmedabad",
            "Rajkot", "Gandhinagar", "Bhavnagar"
        ));
    }

    public static List<String> getStates() {
        return Collections.unmodifiableList(
            Arrays.asList(STATE_CITY_MAP.keySet().toArray(new String[0])));
    }

    public static List<String> getCitiesForState(String state) {
        List<String> cities = STATE_CITY_MAP.get(state);
        return cities != null ? cities : Collections.emptyList();
    }

    public static String getStateForCity(String city) {
        for (Map.Entry<String, List<String>> entry : STATE_CITY_MAP.entrySet()) {
            for (String c : entry.getValue()) {
                if (c.equalsIgnoreCase(city)) return entry.getKey();
            }
        }
        return "";
    }

    public static List<String> getGroups() {
        return Arrays.asList(
            "None", "Family", "Friends", "Work",
            "College", "Business", "Neighbour", "Other"
        );
    }
}

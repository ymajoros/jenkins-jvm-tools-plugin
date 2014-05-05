package org.jenkinsci.plugins.jvmtools;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ym
 */
public class FlightRecordingRepository {

    private static final Map<String, String> shortNameToCanonicalNameMap = new HashMap<>();

    public static String get(String shortName) {
        return shortNameToCanonicalNameMap.get(shortName);
    }

    public static String put(String shortName, String canonicalName) {
        return shortNameToCanonicalNameMap.put(shortName, canonicalName);
    }

    public static String remove(String shortName) {
        return shortNameToCanonicalNameMap.remove(shortName);
    }

}

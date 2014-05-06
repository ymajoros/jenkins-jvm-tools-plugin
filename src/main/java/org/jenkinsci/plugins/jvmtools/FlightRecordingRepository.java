package org.jenkinsci.plugins.jvmtools;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ym
 */
public class FlightRecordingRepository {

    private static final Map<String, String> shortNameToCanonicalNameMap = new HashMap<>();
    private static final Map<String, JvmConfigItem> shortNameToJvmConfigMap = new HashMap<>();

    public static String getCanonicalName(String shortName) {
        return shortNameToCanonicalNameMap.get(shortName);
    }

    public static String saveCanonicalName(String shortName, String canonicalName) {
        return shortNameToCanonicalNameMap.put(shortName, canonicalName);
    }

    public static JvmConfigItem getJvmConfigItem(String shortName) {
        return shortNameToJvmConfigMap.get(shortName);
    }

    public static JvmConfigItem saveJvmConfigItem(String shortName, JvmConfigItem jvmConfigItem) {
        return shortNameToJvmConfigMap.put(shortName, jvmConfigItem);
    }

    public static void remove(String shortName) {
        shortNameToCanonicalNameMap.remove(shortName);
        shortNameToJvmConfigMap.remove(shortName);
    }
    
    

}

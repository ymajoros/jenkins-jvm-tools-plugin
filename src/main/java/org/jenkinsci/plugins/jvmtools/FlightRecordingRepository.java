package org.jenkinsci.plugins.jvmtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ym
 */
public class FlightRecordingRepository {

    private static final ThreadLocal<List<FlightRecording>> currentFlightRecordingsThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, FlightRecording>> flightRecordingMapThreadLocal = new ThreadLocal<>();

    private static Map<String, FlightRecording> getFlightRecordingMap() {
        Map<String, FlightRecording> flightRecordingMap = flightRecordingMapThreadLocal.get();
        if (flightRecordingMap == null) {
            flightRecordingMap = Collections.synchronizedMap(new HashMap<String, FlightRecording>());
            flightRecordingMapThreadLocal.set(flightRecordingMap);
        }
        return flightRecordingMap;
    }

    public static List<FlightRecording> getCurrentFlightRecordings() {
        List<FlightRecording> currentFlightRecordings = currentFlightRecordingsThreadLocal.get();
        if (currentFlightRecordings == null) {
            currentFlightRecordings = new ArrayList<>();
            currentFlightRecordingsThreadLocal.set(currentFlightRecordings);
        }
        return currentFlightRecordings;
    }

    public static void add(FlightRecording flightRecording) {
        List<FlightRecording> flightRecordings = getCurrentFlightRecordings();
        flightRecordings.add(flightRecording);

        String instanceName = flightRecording.getInstanceName();
        if (instanceName != null) {
            Map<String, FlightRecording> flightRecordingMap = getFlightRecordingMap();
            flightRecordingMap.put(instanceName, flightRecording);
        }
    }

    public static void remove(FlightRecording flightRecording) {
        List<FlightRecording> flightRecordings = getCurrentFlightRecordings();
        flightRecordings.remove(flightRecording);

        String instanceName = flightRecording.getInstanceName();
        if (instanceName != null) {
            Map<String, FlightRecording> flightRecordingMap = getFlightRecordingMap();
            flightRecordingMap.remove(instanceName, flightRecording);
        }
    }

    public static FlightRecording findFlightRecording(String instanceName) {
        Map<String, FlightRecording> flightRecordingMap = getFlightRecordingMap();

        return flightRecordingMap.get(instanceName);
    }

}

package org.jenkinsci.plugins.jvmtools;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

/**
 *
 * @author Yannick Majoros
 */
public class JRockitDiagnosticService {

    public final static String JROCKIT_PERFCOUNTER_MBEAN_NAME = "oracle.jrockit.management:type=PerfCounters";
    public final static String JROCKIT_FLIGHT_RECORDER_MBEAN_NAME = "com.oracle.jrockit:type=FlightRecorder";
    public final static String JROCKIT_FLIGHT_RECORDING_MBEAN_NAME_TEMPLATE = "com.oracle.jrockit:type=FlightRecording,id={0}";
    public final static String DIAGNOSTIC_COMMAND_MBEAN_NAME = "oracle.jrockit.management:type=DiagnosticCommand";
    private static final long JFR_DUMP_CHUNK_SIZE = 65535;

    private final RemoteCommandService remoteCommandService;

    public JRockitDiagnosticService(JMXConnector jmxConnector) {
        remoteCommandService = new RemoteCommandService(jmxConnector);
    }

    public String threadDump() throws JMException, IOException {
        String result = (String) remoteCommandService.executeCommand(DIAGNOSTIC_COMMAND_MBEAN_NAME, "execute", new String[]{String.class.getName()}, "print_threads");
        return result;
    }

    public String createFlightRecording() throws IOException, JMException {
        ObjectName objectName = (ObjectName) remoteCommandService.executeCommand(JROCKIT_FLIGHT_RECORDER_MBEAN_NAME, "createRecording", new String[]{String.class.getName()}, new Object[]{null});
        return objectName.getCanonicalName();
    }

    public void startFlightRecording(String canonicalName) throws JMException, IOException {
        remoteCommandService.executeCommand(canonicalName, "start", new String[]{});
    }

    public void stopFlightRecording(String canonicalName) throws JMException, IOException {
        remoteCommandService.executeCommand(canonicalName, "stop", new String[]{});
    }

    public void dumpFlightRecording(String canonicalName, Path outputPath) throws JMException, IOException {
        long streamId = (long) remoteCommandService.executeCommand(canonicalName, "openStream", new String[]{});

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            byte[] readBytes;
            while ((readBytes = (byte[]) remoteCommandService.executeCommand(canonicalName, "readStream", new String[]{"long"}, streamId)) != null) {
                outputStream.write(readBytes);
            }
        }

        remoteCommandService.executeCommand(canonicalName, "closeStream", new String[]{"long"}, streamId);
    }

    public void closeFlightRecording(String canonicalName) throws JMException, IOException {
        remoteCommandService.executeCommand(canonicalName, "close", new String[]{});
    }

}

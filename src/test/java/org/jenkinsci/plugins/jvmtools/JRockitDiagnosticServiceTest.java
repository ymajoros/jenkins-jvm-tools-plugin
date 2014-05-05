package org.jenkinsci.plugins.jvmtools;

import org.jenkinsci.plugins.jvmtools.JRockitDiagnosticService;
import org.jenkinsci.plugins.jvmtools.SimpleJMXConnectorFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.management.remote.JMXConnector;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ym
 */
public class JRockitDiagnosticServiceTest {

    private JMXConnector jmxConnector;
    private JRockitDiagnosticService jRockitDiagnosticService;

    @Before
    public void setup() throws IOException {
        jmxConnector = SimpleJMXConnectorFactory.createJMXConnector("sasha.extranetdc.be", 3035, null, null);
        jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);
    }

    @Test
    public void testThreadDump() throws Exception {
        String threadDump = jRockitDiagnosticService.threadDump();
        System.out.println(threadDump);
    }

    @Test
    public void testFlightRecording() throws Exception {
        // create flight recording
        String flightRecordingCanonicalName = jRockitDiagnosticService.createFlightRecording();

        // start it
        jRockitDiagnosticService.startFlightRecording(flightRecordingCanonicalName);

        Thread.sleep(25000);

        // stop it
        jRockitDiagnosticService.stopFlightRecording(flightRecordingCanonicalName);

        // dump
        Path tempFilePath = Files.createTempFile("jfr_", ".jfr");
        System.out.println(tempFilePath);
        jRockitDiagnosticService.dumpFlightRecording(flightRecordingCanonicalName, tempFilePath);

        // close
        jRockitDiagnosticService.closeFlightRecording(flightRecordingCanonicalName);
    }
}

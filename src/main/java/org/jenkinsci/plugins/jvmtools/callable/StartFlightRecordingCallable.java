package org.jenkinsci.plugins.jvmtools.callable;

import hudson.model.BuildListener;
import hudson.remoting.Callable;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import javax.annotation.Nonnull;
import javax.management.JMException;
import javax.management.remote.JMXConnector;
import org.jenkinsci.plugins.jvmtools.FlightRecording;
import org.jenkinsci.plugins.jvmtools.FlightRecordingRepository;
import org.jenkinsci.plugins.jvmtools.JRockitDiagnosticService;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.SimpleJMXConnectorFactory;

/**
 *
 * @author ym
 */
public class StartFlightRecordingCallable implements Callable<Void, RuntimeException> {

    @Nonnull
    private final FlightRecording flightRecording;
    private final BuildListener listener;

    public StartFlightRecordingCallable(@Nonnull FlightRecording flightRecording, @Nonnull BuildListener listener) {
        this.flightRecording = flightRecording;
        this.listener = listener;
    }

    @Override
    public Void call() {
        PrintStream logger = listener.getLogger();
        try {
            JvmConfigItem jvmConfigItem = flightRecording.getJvmConfigItem();

            String hostName = jvmConfigItem.getHostName();
            int port = jvmConfigItem.getPort();
            String userName = jvmConfigItem.getUserName();
            String password = jvmConfigItem.getPassword();
            String startingMessage = MessageFormat.format("Starting flight recording at {0}:{1,number,0}", hostName, port);
            logger.println(startingMessage);
            JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
            JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);

            // create flight recording
            String canonicalName = jRockitDiagnosticService.createFlightRecording();

            // register (for use in stop / dump etc.)
            flightRecording.setCanonicalName(canonicalName);
            FlightRecordingRepository.add(flightRecording);

            // start it
            jRockitDiagnosticService.startFlightRecording(canonicalName);
            String startedMessage = MessageFormat.format("Flight recording started with remote canonical name {0}", canonicalName);
            logger.println(startedMessage);

            return null;
        } catch (IOException | JMException exception) {
            throw new RuntimeException(exception);
        }
    }

}

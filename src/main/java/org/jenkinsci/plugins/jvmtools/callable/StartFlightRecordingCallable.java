package org.jenkinsci.plugins.jvmtools.callable;

import hudson.model.BuildListener;
import hudson.remoting.Callable;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import javax.management.JMException;
import javax.management.remote.JMXConnector;
import org.jenkinsci.plugins.jvmtools.FlightRecordingRepository;
import org.jenkinsci.plugins.jvmtools.JRockitDiagnosticService;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.SimpleJMXConnectorFactory;

/**
 *
 * @author ym
 */
public class StartFlightRecordingCallable implements Callable<Void, RuntimeException> {

    private final String instanceName;
    private final JvmConfigItem jvmConfigItem;
    private final BuildListener listener;

    public StartFlightRecordingCallable(String instanceName, JvmConfigItem jvmConfigItem, BuildListener listener) {
        this.instanceName = instanceName;
        this.jvmConfigItem = jvmConfigItem;
        this.listener = listener;
    }

    @Override
    public Void call() {
        PrintStream logger = listener.getLogger();
        try {
            String hostName = jvmConfigItem.getHostName();
            int port = jvmConfigItem.getPort();
            String userName = jvmConfigItem.getUserName();
            String password = jvmConfigItem.getPassword();
            String startingMessage = MessageFormat.format("Starting flight recording at {0}:{1,number,0}", hostName, port);
            logger.println(startingMessage);
            JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
            JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);
            // create flight recording
            String flightRecordingCanonicalName = jRockitDiagnosticService.createFlightRecording();
            // register (for use in stop / dump etc.)
            FlightRecordingRepository.saveCanonicalName(instanceName, flightRecordingCanonicalName);
            FlightRecordingRepository.saveJvmConfigItem(instanceName, jvmConfigItem);
            // start it
            jRockitDiagnosticService.startFlightRecording(flightRecordingCanonicalName);
            String startedMessage = MessageFormat.format("Flight recording started with remote canonical name {0}", flightRecordingCanonicalName);
            logger.println(startedMessage);
            return null;
        } catch (IOException | JMException exception) {
            throw new RuntimeException(exception);
        }
    }

}

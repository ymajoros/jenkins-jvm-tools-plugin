package org.jenkinsci.plugins.jvmtools.callable;

import hudson.model.BuildListener;
import hudson.remoting.Callable;
import java.io.IOException;
import java.io.PrintStream;
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
public class StopFlightRecoringCallable implements Callable<Void, Exception> {

    private final String instanceName;
    private final BuildListener listener;

    public StopFlightRecoringCallable(String instanceName, BuildListener listener) {
        this.instanceName = instanceName;
        this.listener = listener;
    }

    @Override
    public Void call() throws JMException, IOException {
        PrintStream logger = listener.getLogger();
        // find flight recording
        FlightRecording flightRecording = FlightRecordingRepository.findFlightRecording(instanceName);
        String flightRecordingCanonicalName = flightRecording.getCanonicalName();
        JvmConfigItem jvmConfigItem = flightRecording.getJvmConfigItem();
        String hostName = jvmConfigItem.getHostName();
        int port = jvmConfigItem.getPort();
        String userName = jvmConfigItem.getUserName();
        String password = jvmConfigItem.getPassword();
        // connect
        JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
        JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);
        // stop it
        jRockitDiagnosticService.stopFlightRecording(flightRecordingCanonicalName);
        logger.println("Flight recording stopped");
        return null;
    }

}

package org.jenkinsci.plugins.jvmtools.callable;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class DumpFlightRecordingCallable implements Callable<Void, Exception> {

    private final String instanceName;
    private final FilePath workingDir;
    private final BuildListener listener;
    private final boolean stop;
    private final boolean close;
    private final String fileName;

    public DumpFlightRecordingCallable(String instanceName, FilePath workingDir, BuildListener listener, boolean stop, boolean close, String fileName) {
        this.instanceName = instanceName;
        this.workingDir = workingDir;
        this.listener = listener;
        this.stop = stop;
        this.close = close;
        this.fileName = fileName;
    }

    @Override
    public Void call() throws JMException, IOException {
        PrintStream logger = listener.getLogger();

        // find flight recording
        String flightRecordingCanonicalName = FlightRecordingRepository.getCanonicalName(instanceName);
        JvmConfigItem jvmConfigItem = FlightRecordingRepository.getJvmConfigItem(instanceName);
        if (jvmConfigItem == null) {
            throw new RuntimeException("JVM config not found");
        }
        String hostName = jvmConfigItem.getHostName();
        int port = jvmConfigItem.getPort();
        String userName = jvmConfigItem.getUserName();
        String password = jvmConfigItem.getPassword();
        // connect
        JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
        JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);
        // stop it
        if (stop) {
            jRockitDiagnosticService.stopFlightRecording(flightRecordingCanonicalName);
            logger.println("Flight recording stopped");
        }
        Path path = Paths.get(fileName);
        if (!path.isAbsolute()) {
            String workspaceDirName = workingDir.getRemote();
            path = Paths.get(workspaceDirName, fileName);
        }
        Path absolutePath = path.toAbsolutePath();
        jRockitDiagnosticService.dumpFlightRecording(flightRecordingCanonicalName, absolutePath);
        String message = MessageFormat.format("Flight recording dumped to {0}", absolutePath);
        logger.println(message);
        // close it
        if (close) {
            jRockitDiagnosticService.closeFlightRecording(flightRecordingCanonicalName);
            logger.println("Flight recording closed");
        }
        return null;
    }

}

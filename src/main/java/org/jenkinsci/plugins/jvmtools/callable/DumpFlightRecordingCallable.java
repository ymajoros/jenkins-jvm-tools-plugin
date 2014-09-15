package org.jenkinsci.plugins.jvmtools.callable;

import hudson.FilePath;
import hudson.remoting.Callable;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import javax.management.JMException;
import javax.management.remote.JMXConnector;
import org.jenkinsci.plugins.jvmtools.FlightRecording;
import org.jenkinsci.plugins.jvmtools.JRockitDiagnosticService;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.SimpleJMXConnectorFactory;

/**
 *
 * @author ym
 */
public class DumpFlightRecordingCallable implements Callable<Void, Exception> {

    private final FlightRecording flightRecording;
    private final FilePath workingDir;
    private final boolean stop;
    private final boolean close;
    private final PrintStream logger;

    public DumpFlightRecordingCallable(FlightRecording flightRecording, FilePath workingDir, boolean stop, boolean close, PrintStream logger) {
        this.flightRecording = flightRecording;
        this.workingDir = workingDir;
        this.stop = stop;
        this.close = close;
        this.logger = logger;
    }

    @Override
    public Void call() throws JMException, IOException {
        JvmConfigItem jvmConfigItem = flightRecording.getJvmConfigItem();
        String canonicalName = flightRecording.getCanonicalName();
        String fileName = flightRecording.getFileName();

        // find flight recording
        String hostName = jvmConfigItem.getHostName();
        int port = jvmConfigItem.getPort();
        String userName = jvmConfigItem.getUserName();
        String password = jvmConfigItem.getPassword();

        // connect
        JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
        JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);

        // stop it
        if (stop) {
            jRockitDiagnosticService.stopFlightRecording(canonicalName);
            logger.println("Flight recording stopped");
        }
        Path path = Paths.get(fileName);
        if (!path.isAbsolute()) {
            String workspaceDirName = workingDir.getRemote();
            path = Paths.get(workspaceDirName, fileName);
        }
        Path absolutePath = path.toAbsolutePath();
        jRockitDiagnosticService.dumpFlightRecording(canonicalName, absolutePath);
        String message = MessageFormat.format("Flight recording dumped to {0}", absolutePath);
        logger.println(message);

        // close it
        if (close) {
            jRockitDiagnosticService.closeFlightRecording(canonicalName);
            logger.println("Flight recording closed");
        }
        return null;
    }

}

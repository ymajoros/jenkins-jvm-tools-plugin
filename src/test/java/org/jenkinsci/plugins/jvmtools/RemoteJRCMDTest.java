package org.jenkinsci.plugins.jvmtools;

import org.jenkinsci.plugins.jvmtools.JRockitDiagnosticService;
import org.jenkinsci.plugins.jvmtools.SimpleJMXConnectorFactory;
import org.jenkinsci.plugins.jvmtools.RemoteCommandService;
import java.io.IOException;
import javax.management.remote.JMXConnector;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Yannick Majoros
 */
public class RemoteJRCMDTest {

    private JMXConnector jmxConnector;
    private RemoteCommandService remoteCommandService;

    @Before
    public void setup() throws IOException {
        jmxConnector = SimpleJMXConnectorFactory.createJMXConnector("sasha.extranetdc.be", 3035, null, null);
        remoteCommandService = new RemoteCommandService(jmxConnector);
    }

    @Test
    public void testExecuteCommand() throws Exception {
        Object result = remoteCommandService.executeCommand(JRockitDiagnosticService.DIAGNOSTIC_COMMAND_MBEAN_NAME, "execute", new String[]{"java.lang.String"}, "print_threads");
        System.out.println(result);
//        remoteJRCMD.executeCommand("sasha.extranetdc.be", 3035, null, null, "start_flightrecording", "duration=10s");
    }

}

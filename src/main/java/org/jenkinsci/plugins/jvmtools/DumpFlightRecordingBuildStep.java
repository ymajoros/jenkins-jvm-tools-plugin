package org.jenkinsci.plugins.jvmtools;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.JMException;
import javax.management.remote.JMXConnector;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Yannick Majoros
 */
public class DumpFlightRecordingBuildStep extends Builder {

    private static final Logger log = Logger.getLogger(DumpFlightRecordingBuildStep.class.getName());

    private final String hostName;
    private final int port;
    private final String userName;
    private final String password;
    private final String instanceName;
    private final String fileName;
    private final boolean stop;
    private final boolean close;

    /**
     * The constructor used at form submission
     *
     * @param hostName
     * @param port
     * @param userName
     * @param password
     * @param instanceName
     * @param fileName
     * @param stop
     * @param close
     */
    @DataBoundConstructor
    public DumpFlightRecordingBuildStep(String hostName, int port, String userName, String password, String instanceName, String fileName, boolean stop, boolean close) {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.instanceName = instanceName;
        this.fileName = fileName;
        this.stop = stop;
        this.close = close;
    }

    //<editor-fold defaultstate="collapsed" desc="get/set...">
    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean getStop() {
        return stop;
    }

    public boolean getClose() {
        return close;
    }
    //</editor-fold>

    /**
     * Perform the build step on the execution host.
     * <p>
     * Generates a temporary file and copies the content of the predefined
     * config file (by using the buildStepId) into it. It then copies this file
     * into the workspace directory of the execution host and executes it.
     *
     * @param build
     * @param launcher
     * @param listener
     * @return
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        try {
            listener.getLogger().println("Dumping flight recording");

            JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
            JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);

            // find flight recording
            String flightRecordingCanonicalName = FlightRecordingRepository.get(instanceName);

            // stop it
            if (stop) {
                jRockitDiagnosticService.stopFlightRecording(flightRecordingCanonicalName);
                log.log(Level.FINE, "Flight recording stopped");
            }

            Path path = Paths.get(fileName);
            Path absolutePath = path.toAbsolutePath();
            jRockitDiagnosticService.dumpFlightRecording(flightRecordingCanonicalName, absolutePath);

            String message = MessageFormat.format("Flight recording dumped to {0}", absolutePath);
            listener.getLogger().println(message);

            // close it
            if (close) {
                jRockitDiagnosticService.closeFlightRecording(flightRecordingCanonicalName);
                log.log(Level.FINE, "Flight recording closed");
            }

            return true;
        } catch (IOException | JMException exception) {
            throw new RuntimeException(exception);
        }
    }

    // Overridden for better type safety.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link StartFlightRecordingBuildStep}.
     */
    @Extension(ordinal = 50)
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        final Logger logger = Logger.getLogger(DumpFlightRecordingBuildStep.class.getName());

        /**
         * Enables this builder for all kinds of projects.
         *
         * @param aClass
         * @return
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         *
         * @return
         */
        @Override
        public String getDisplayName() {
            return Messages.dump_fr_buildstep_name();
        }

        /**
         * validate that an existing config was chosen
         *
         * @param hostname
         * @param port
         * @param user
         * @param password
         * @param maxDuration
         * @param instanceName
         * @param fileName
         * @param stop
         * @param close
         * @return
         */
        public FormValidation doCheckBuildStepId(@QueryParameter String hostname, @QueryParameter int port, @QueryParameter String user, @QueryParameter String password, @QueryParameter long maxDuration, @QueryParameter String instanceName, @QueryParameter String fileName, @QueryParameter boolean stop, @QueryParameter boolean close) {
            return FormValidation.ok();
//                return FormValidation.error("you must select a valid script");
        }

    }
}

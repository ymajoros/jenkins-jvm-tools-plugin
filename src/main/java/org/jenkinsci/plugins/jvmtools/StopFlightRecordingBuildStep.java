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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.JMException;
import javax.management.remote.JMXConnector;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Yannick Majoros
 */
public class StopFlightRecordingBuildStep extends Builder {

    private static final Logger log = Logger.getLogger(StopFlightRecordingBuildStep.class.getName());

    private final String instanceName;

    /**
     * The constructor used at form submission
     *
     * @param instanceName
     */
    @DataBoundConstructor
    public StopFlightRecordingBuildStep(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceName() {
        return instanceName;
    }

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
            listener.getLogger().println("Stopping flight recording");

            // find flight recording
            String flightRecordingCanonicalName = FlightRecordingRepository.getCanonicalName(instanceName);
            JvmConfigItem jvmConfigItem = FlightRecordingRepository.getJvmConfigItem(instanceName);

            String hostName = jvmConfigItem.getHostName();
            int port = jvmConfigItem.getPort();
            String userName = jvmConfigItem.getUserName();
            String password = jvmConfigItem.getPassword();

            // connect
            JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
            JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);

            // stop it
            jRockitDiagnosticService.stopFlightRecording(flightRecordingCanonicalName);

            log.log(Level.FINE, "Flight recording stopped");

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

        final Logger logger = Logger.getLogger(StopFlightRecordingBuildStep.class.getName());

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
            return Messages.stop_fr_buildstep_name();
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
         * @return
         */
        public FormValidation doCheckBuildStepId(@QueryParameter String hostname, @QueryParameter int port, @QueryParameter String user, @QueryParameter String password, @QueryParameter long maxDuration, @QueryParameter String instanceName) {
            return FormValidation.ok();
//                return FormValidation.error("you must select a valid script");
        }

    }
}

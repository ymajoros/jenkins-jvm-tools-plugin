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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.JMException;
import javax.management.remote.JMXConnector;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * A project that uses this builder can choose a build step from a list of
 * predefined config files that are uses as command line scripts. The hash-bang
 * sequence at the beginning of each file is used to determine the interpreter.
 *
 * @author Yannick Majoros
 */
public class StartFlightRecordingBuildStep extends Builder {

    private static final Logger log = Logger.getLogger(StartFlightRecordingBuildStep.class.getName());

    private final String hostName;
    private final int port;
    private final String userName;
    private final String password;
    private final Long maxDuration;
    private final String instanceName;

    /**
     * The constructor used at form submission
     *
     * @param hostName
     * @param port
     * @param userName
     * @param password
     * @param maxDuration
     * @param instanceName
     */
    @DataBoundConstructor
    public StartFlightRecordingBuildStep(String hostName, int port, String userName, String password, Long maxDuration, String instanceName) {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.maxDuration = maxDuration;
        this.instanceName = instanceName;
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

    public long getMaxDuration() {
        return maxDuration;
    }

    public String getInstanceName() {
        return instanceName;
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
            listener.getLogger().println("Starting flight recording");

            JMXConnector jmxConnector = SimpleJMXConnectorFactory.createJMXConnector(hostName, port, userName, password);
            JRockitDiagnosticService jRockitDiagnosticService = new JRockitDiagnosticService(jmxConnector);

            // create flight recording
            String flightRecordingCanonicalName = jRockitDiagnosticService.createFlightRecording();

            // register (for use in stop / dump etc.)
            FlightRecordingRepository.put(instanceName, flightRecordingCanonicalName);

            // start it
            jRockitDiagnosticService.startFlightRecording(flightRecordingCanonicalName);

            log.log(Level.FINE, "Flight recording started");

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

        final Logger logger = Logger.getLogger(StartFlightRecordingBuildStep.class.getName());

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
            return Messages.start_fr_buildstep_name();
        }

        /**
         * validate that an existing config was chosen
         *
         * @param hostName
         * @param port
         * @param userName
         * @param password
         * @param maxDuration
         * @param instanceName
         * @return
         */
        public FormValidation doCheckBuildStepId(@QueryParameter String hostName, @QueryParameter int port, @QueryParameter String userName, @QueryParameter String password, @QueryParameter Long maxDuration, @QueryParameter String instanceName) {
            return FormValidation.ok();
//                return FormValidation.error("you must select a valid script");
        }

        /**
         * Return all jvm configurations that the user can choose from when
         * creating a build step. Ordered by name.
         *
         * @return A collection of jvm configurations of type
         * {@link JvmConfigItem}.
         */
        public Collection<JvmConfigItem> getAvailableJvmConfigItems() {
            JvmConfig jvmConfig = JvmConfig.get();
            List<JvmConfigItem> jvmConfigItems = jvmConfig.getJvmConfigItems();

            Collections.sort(jvmConfigItems, new Comparator<JvmConfigItem>() {

                @Override
                public int compare(JvmConfigItem jvmConfigItem1, JvmConfigItem jvmConfigItem2) {
                    String name1 = jvmConfigItem1.getName();
                    String name2 = jvmConfigItem2.getName();
                    return name1.compareToIgnoreCase(name2);
                }
            });

            return jvmConfigItems;
        }
    }
}

package org.jenkinsci.plugins.jvmtools;

import org.jenkinsci.plugins.jvmtools.callable.StopFlightRecoringCallable;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

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
        PrintStream logger = listener.getLogger();
        logger.println("Stopping flight recording");

        Callable<Void, Exception> callable = new StopFlightRecoringCallable(instanceName, listener);

        try {
            launcher.getChannel().call(callable);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (InterruptedException interruptedException) {
            logger.println("Interrupted.");
            return false;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return true;
    }

    // Overridden for better type safety.
    @Override
    public StopFlightRecordingBuildStepDescriptor getDescriptor() {
        return (StopFlightRecordingBuildStepDescriptor) super.getDescriptor();
    }

    /**
     * Descriptor for {@link StartFlightRecordingBuildStep}.
     */
    @Extension
    public static final class StopFlightRecordingBuildStepDescriptor extends BuildStepDescriptor<Builder> {

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

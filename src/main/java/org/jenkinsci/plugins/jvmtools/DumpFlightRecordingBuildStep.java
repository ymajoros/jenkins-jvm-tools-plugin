package org.jenkinsci.plugins.jvmtools;

import org.jenkinsci.plugins.jvmtools.callable.DumpFlightRecordingCallable;
import hudson.Extension;
import hudson.FilePath;
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

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Yannick Majoros
 */
public class DumpFlightRecordingBuildStep extends Builder {

    private final String instanceName;
    private final String fileName;
    private final boolean stop;
    private final boolean close;

    /**
     * The constructor used at form submission
     *
     * @param instanceName
     * @param fileName
     * @param stop
     * @param close
     */
    @DataBoundConstructor
    public DumpFlightRecordingBuildStep(String instanceName, String fileName, boolean stop, boolean close) {
        this.instanceName = instanceName;
        this.fileName = fileName;
        this.stop = stop;
        this.close = close;
    }

    //<editor-fold defaultstate="collapsed" desc="get/set...">
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
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) {
        final PrintStream logger = listener.getLogger();
        logger.println("Dumping flight recording");

        FilePath workingDir = build.getWorkspace();

        Callable<Void, Exception> callable = new DumpFlightRecordingCallable(instanceName, workingDir, listener, stop, close, fileName);

        try {
            launcher.getChannel().call(callable);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (InterruptedException ex) {
            logger.println("Interrupted.");
            return false;
        } catch (Exception ioException) {
            throw new RuntimeException(ioException);
        }

        return true;
    }

    // Overridden for better type safety.
    @Override
    public DumpFlightRecordingBuildStepDescriptor getDescriptor() {
        return (DumpFlightRecordingBuildStepDescriptor) super.getDescriptor();
    }

    /**
     * Descriptor for {@link StartFlightRecordingBuildStep}.
     */
    @Extension
    public static final class DumpFlightRecordingBuildStepDescriptor extends BuildStepDescriptor<Builder> {

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

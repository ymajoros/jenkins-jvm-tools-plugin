package org.jenkinsci.plugins.jvmtools.build;

import org.jenkinsci.plugins.jvmtools.build.descriptor.StopFlightRecordingBuildStepDescriptor;
import org.jenkinsci.plugins.jvmtools.callable.StopFlightRecoringCallable;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

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
            VirtualChannel virtualChannel = launcher.getChannel();
            virtualChannel.call(callable);
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


}

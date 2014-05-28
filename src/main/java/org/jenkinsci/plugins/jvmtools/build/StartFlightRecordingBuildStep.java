package org.jenkinsci.plugins.jvmtools.build;

import org.jenkinsci.plugins.jvmtools.build.descriptor.StartFlightRecordingBuildStepDescriptor;
import org.jenkinsci.plugins.jvmtools.callable.StartFlightRecordingCallable;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import java.io.IOException;
import java.io.PrintStream;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A project that uses this builder can choose a build step from a list of
 * predefined config files that are uses as command line scripts. The hash-bang
 * sequence at the beginning of each file is used to determine the interpreter.
 *
 * @author Yannick Majoros
 */
public class StartFlightRecordingBuildStep extends Builder {

    private final String jvmConfigName;
    private final Long maxDuration;
    private final String instanceName;

    /**
     * The constructor used at form submission
     *
     * @param jvmConfigName
     * @param maxDuration
     * @param instanceName
     */
    @DataBoundConstructor
    public StartFlightRecordingBuildStep(String jvmConfigName, Long maxDuration, String instanceName) {
        this.jvmConfigName = jvmConfigName;
        this.maxDuration = maxDuration;
        this.instanceName = instanceName;
    }

    //<editor-fold defaultstate="collapsed" desc="get/set...">
    public String getJvmConfigName() {
        return jvmConfigName;
    }

    public Long getMaxDuration() {
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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) {
        PrintStream logger = listener.getLogger();
        StartFlightRecordingBuildStepDescriptor descriptor = getDescriptor();
        JvmConfigItem jvmConfigItem = descriptor.getBuildStepConfigByName(jvmConfigName);
        if (jvmConfigItem == null) {
            logger.println(Messages.config_does_not_exist(jvmConfigName));
            return false;
        }

        StartFlightRecordingCallable startFlightRecordingCallable = new StartFlightRecordingCallable(instanceName, jvmConfigItem, listener);

        try {
            VirtualChannel virtualChannel = launcher.getChannel();
            virtualChannel.call(startFlightRecordingCallable);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (InterruptedException interruptedException) {
            logger.println("Interrupted.");
            return false;
        }

        return true;
    }

    @Override
    public StartFlightRecordingBuildStepDescriptor getDescriptor() {
        return (StartFlightRecordingBuildStepDescriptor) super.getDescriptor();
    }


}

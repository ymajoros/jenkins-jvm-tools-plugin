package org.jenkinsci.plugins.jvmtools.build;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Environment;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import hudson.tasks.Recorder;
import java.io.IOException;
import java.io.PrintStream;
import org.jenkinsci.plugins.jvmtools.FlightRecording;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.Messages;
import org.jenkinsci.plugins.jvmtools.build.descriptor.FlightRecordingCycleBuildStepDescriptor;
import org.jenkinsci.plugins.jvmtools.callable.DumpFlightRecordingCallable;
import org.jenkinsci.plugins.jvmtools.callable.StartFlightRecordingCallable;
import org.jenkinsci.plugins.jvmtools.util.JvmConfigUtil;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Yannick Majoros
 */
public class FlightRecordingCycleBuildStep extends Builder {

    private final String jvmConfigName;
    private final Long maxDuration;
    private final String fileName;

    /**
     * The constructor used at form submission
     *
     * @param jvmConfigName
     * @param maxDuration
     * @param fileName
     */
    @DataBoundConstructor
    public FlightRecordingCycleBuildStep(String jvmConfigName, Long maxDuration, String fileName) {
        this.jvmConfigName = jvmConfigName;
        this.maxDuration = maxDuration;
        this.fileName = fileName;
    }

    //<editor-fold defaultstate="collapsed" desc="get/set...">
    public String getFileName() {
        return fileName;
    }

    public String getJvmConfigName() {
        return jvmConfigName;
    }

    public Long getMaxDuration() {
        return maxDuration;
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
        PrintStream logger = listener.getLogger();

        JvmConfigItem jvmConfigItem = JvmConfigUtil.getBuildStepConfigByName(jvmConfigName);
        if (jvmConfigItem == null) {
            logger.println(Messages.config_does_not_exist(jvmConfigName));
            return false;
        }

        FlightRecording flightRecording = new FlightRecording(null, jvmConfigItem, fileName);
        StartFlightRecordingCallable startFlightRecordingCallable = new StartFlightRecordingCallable(flightRecording, listener);

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

    // Overridden for better type safety.
    @Override
    public FlightRecordingCycleBuildStepDescriptor getDescriptor() {
        return (FlightRecordingCycleBuildStepDescriptor) super.getDescriptor();
    }

}

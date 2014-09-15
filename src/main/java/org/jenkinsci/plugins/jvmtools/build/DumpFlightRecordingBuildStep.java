package org.jenkinsci.plugins.jvmtools.build;

import org.jenkinsci.plugins.jvmtools.build.descriptor.DumpFlightRecordingBuildStepDescriptor;
import org.jenkinsci.plugins.jvmtools.callable.DumpFlightRecordingCallable;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import java.io.IOException;
import java.io.PrintStream;
import org.jenkinsci.plugins.jvmtools.FlightRecording;
import org.jenkinsci.plugins.jvmtools.FlightRecordingRepository;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;

import org.kohsuke.stapler.DataBoundConstructor;

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

        FlightRecording flightRecording = FlightRecordingRepository.findFlightRecording(instanceName);
        flightRecording.setFileName(fileName);
        Callable<Void, Exception> callable = new DumpFlightRecordingCallable(flightRecording, workingDir, stop, close, logger);

        try {
            VirtualChannel virtualChannel = launcher.getChannel();
            virtualChannel.call(callable);
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

}

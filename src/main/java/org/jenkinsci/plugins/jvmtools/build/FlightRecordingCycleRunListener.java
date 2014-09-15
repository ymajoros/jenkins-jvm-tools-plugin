package org.jenkinsci.plugins.jvmtools.build;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.jvmtools.FlightRecording;
import org.jenkinsci.plugins.jvmtools.FlightRecordingRepository;
import org.jenkinsci.plugins.jvmtools.callable.DumpFlightRecordingCallable;

/**
 *
 * @author ym
 */
@Extension
public class FlightRecordingCycleRunListener extends RunListener<Run<?, ?>> {

    @Override
    public Environment setUpEnvironment(final AbstractBuild build, final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException, Run.RunnerAbortedException {
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                PrintStream logger = listener.getLogger();

                List<FlightRecording> flightRecordings = FlightRecordingRepository.getCurrentFlightRecordings();
                try {
                    try {
                        for (FlightRecording flightRecording : flightRecordings) {
                            FilePath workingDir = build.getWorkspace();

                            String fileName = flightRecording.getFileName();
                            if (fileName != null) {
                                DumpFlightRecordingCallable dumpFlightRecordingCallable = new DumpFlightRecordingCallable(flightRecording, workingDir, true, true, logger);
                                VirtualChannel virtualChannel = launcher.getChannel();
                                virtualChannel.call(dumpFlightRecordingCallable);
                            }
                        }
                    } finally {
                        flightRecordings = new ArrayList<>(flightRecordings);
                        for (FlightRecording flightRecording : flightRecordings) {
                            FlightRecordingRepository.remove(flightRecording);
                        }
                    }
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
                return true;
            }

        };
    }

}

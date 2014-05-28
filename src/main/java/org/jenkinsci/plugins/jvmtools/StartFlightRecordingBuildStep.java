package org.jenkinsci.plugins.jvmtools;

import org.jenkinsci.plugins.jvmtools.callable.StartFlightRecordingCallable;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
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

    /**
     * Descriptor for {@link StartFlightRecordingBuildStep}.
     */
    @Extension
    public static final class StartFlightRecordingBuildStepDescriptor extends BuildStepDescriptor<Builder> {

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
         * @param jvmConfigName
         * @param maxDuration
         * @param instanceName
         * @return
         */
        public FormValidation doCheckBuildStepId(@QueryParameter String jvmConfigName, @QueryParameter Long maxDuration, @QueryParameter String instanceName) {
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

        private JvmConfigItem getBuildStepConfigByName(String jvmConfigName) {
            Collection<JvmConfigItem> availableJvmConfigItems = getAvailableJvmConfigItems();
            for (JvmConfigItem availableJvmConfigItem : availableJvmConfigItems) {
                String availableJvmConfigItemName = availableJvmConfigItem.getName();
                if (availableJvmConfigItemName.equals(jvmConfigName)) {
                    return availableJvmConfigItem;
                }
            }
            return null;
        }
    }

}

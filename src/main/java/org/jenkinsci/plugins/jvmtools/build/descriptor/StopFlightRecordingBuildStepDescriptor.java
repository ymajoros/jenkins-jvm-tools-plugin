package org.jenkinsci.plugins.jvmtools.build.descriptor;

import hudson.Extension;
import static hudson.init.InitMilestone.PLUGINS_STARTED;
import hudson.init.Initializer;
import hudson.model.AbstractProject;
import hudson.model.Items;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.jvmtools.Messages;
import org.jenkinsci.plugins.jvmtools.build.StartFlightRecordingBuildStep;
import org.jenkinsci.plugins.jvmtools.build.StopFlightRecordingBuildStep;
import org.kohsuke.stapler.QueryParameter;

/**
 * Descriptor for {@link StartFlightRecordingBuildStep}.
 */
@Extension
public final class StopFlightRecordingBuildStepDescriptor extends BuildStepDescriptor<Builder> {

    public StopFlightRecordingBuildStepDescriptor() {
        super(StopFlightRecordingBuildStep.class);
    }

    @Initializer(before = PLUGINS_STARTED)
    public static void addAliases() {
        Items.XSTREAM2.addCompatibilityAlias("org.jenkinsci.plugins.jvmtools.StopFlightRecordingBuildStep", StopFlightRecordingBuildStep.class);
    }

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

package org.jenkinsci.plugins.jvmtools.build.descriptor;

import hudson.Extension;
import static hudson.init.InitMilestone.PLUGINS_STARTED;
import hudson.init.Initializer;
import hudson.model.AbstractProject;
import hudson.model.Items;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.util.Collection;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.Messages;
import org.jenkinsci.plugins.jvmtools.build.StartFlightRecordingBuildStep;
import org.jenkinsci.plugins.jvmtools.util.JvmConfigUtil;
import org.kohsuke.stapler.QueryParameter;

/**
 * Descriptor for {@link StartFlightRecordingBuildStep}.
 */
@Extension
public final class StartFlightRecordingBuildStepDescriptor extends BuildStepDescriptor<Builder> {

    public StartFlightRecordingBuildStepDescriptor() {
        super(StartFlightRecordingBuildStep.class);
    }

    @Initializer(before = PLUGINS_STARTED)
    public static void addAliases() {
        Items.XSTREAM2.addCompatibilityAlias("org.jenkinsci.plugins.jvmtools.StartFlightRecordingBuildStep", StartFlightRecordingBuildStep.class);
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

    public Collection<JvmConfigItem> getAvailableJvmConfigItems() {
        return JvmConfigUtil.getAvailableJvmConfigItems();
    }

}

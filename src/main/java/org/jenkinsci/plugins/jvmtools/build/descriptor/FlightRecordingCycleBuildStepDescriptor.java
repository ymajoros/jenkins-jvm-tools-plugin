package org.jenkinsci.plugins.jvmtools.build.descriptor;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.util.Collection;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.Messages;
import org.jenkinsci.plugins.jvmtools.build.FlightRecordingCycleBuildStep;
import org.jenkinsci.plugins.jvmtools.util.JvmConfigUtil;
import org.kohsuke.stapler.QueryParameter;

/**
 * Descriptor for {@link FlightRecordingCycleBuildStep}.
 */
@Extension
public final class FlightRecordingCycleBuildStepDescriptor extends BuildStepDescriptor<Builder> {

    public FlightRecordingCycleBuildStepDescriptor() {
        super(FlightRecordingCycleBuildStep.class);
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
        return Messages.cycle_fr_buildstep_name();
    }

    /**
     * validate that an existing config was chosen
     *
     * @param jvmConfigName
     * @param maxDuration
     * @param fileName
     * @return
     */
    public FormValidation doCheckBuildStepId(@QueryParameter String jvmConfigName, @QueryParameter Long maxDuration, @QueryParameter String fileName) {
        return FormValidation.ok();
        //                return FormValidation.error("you must select a valid script");
    }

    public Collection<JvmConfigItem> getAvailableJvmConfigItems() {
        return JvmConfigUtil.getAvailableJvmConfigItems();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jenkinsci.plugins.jvmtools.JvmConfig;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;
import org.jenkinsci.plugins.jvmtools.Messages;
import org.jenkinsci.plugins.jvmtools.build.StartFlightRecordingBuildStep;
import org.jenkinsci.plugins.jvmtools.build.StopFlightRecordingBuildStep;
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

    /**
     * Return all jvm configurations that the user can choose from when creating
     * a build step. Ordered by name.
     *
     * @return A collection of jvm configurations of type {@link JvmConfigItem}.
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

    public JvmConfigItem getBuildStepConfigByName(String jvmConfigName) {
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

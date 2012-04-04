package org.jenkinsci.plugins.managedscripts;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import hudson.util.FormValidation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jenkinsci.lib.configprovider.ConfigProvider;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.managedscripts.ScriptConfig.Arg;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * A project that uses this builder can choose a build step from a list of predefined windows batch files that are used as command line scripts.
 * <p>
 * 
 * @author Dominik Bartholdi (imod)
 */
public class WinBatchBuildStep extends CommandInterpreter {

    private static Logger log = Logger.getLogger(WinBatchBuildStep.class.getName());

    private final String buildStepId;
    private final String[] buildStepArgs;

    /**
     * The constructor
     * 
     * @param buildStepId
     *            the Id of the config file
     * @param buildStepArgs
     *            list of arguments specified as buildStepargs
     */
    @DataBoundConstructor
    public WinBatchBuildStep(String buildStepId, String[] buildStepArgs) {
        super("---not a command---"); // not used anywhere...
        this.buildStepId = buildStepId;
        this.buildStepArgs = buildStepArgs;
    }

    public String getBuildStepId() {
        return buildStepId;
    }

    public String[] getBuildStepArgs() {
        return buildStepArgs;
    }

    @Override
    public String[] buildCommandLine(FilePath script) {

        List<String> cml = new ArrayList<String>();
        cml.add("cmd");
        cml.add("/c");
        cml.add("call");
        cml.add(script.getRemote());

        // Add additional parameters set by user
        if (buildStepArgs != null) {
            for (String arg : buildStepArgs) {
                cml.add(arg);
            }
        }

        // return new String[] { "cmd", "/c", "call", script.getRemote() };
        return (String[]) cml.toArray(new String[cml.size()]);
    }

    @Override
    protected String getContents() {
        Config buildStepConfig = getDescriptor().getBuildStepConfigById(buildStepId);
        if (buildStepConfig == null) {
            throw new IllegalStateException("Cannot find batch file with Id '" + buildStepId + "'. Are you sure it exists?");
        }
        return buildStepConfig.content + "\r\nexit %ERRORLEVEL%";
    }

    @Override
    protected String getFileExtension() {
        return ".bat";
    }

    // Overridden for better type safety.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link WinBatchBuildStep}.
     */
    @Extension(ordinal = 55)
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        final Logger logger = Logger.getLogger(WinBatchBuildStep.class.getName());

        /**
         * Enables this builder for all kinds of projects.
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return Messages.win_buildstep_name();
        }

        /**
         * Return all batch files (templates) that the user can choose from when creating a build step. Ordered by name.
         * 
         * @return A collection of batch files of type {@link WinBatchConfig}.
         */
        public Collection<Config> getAvailableBuildTemplates() {
            List<Config> allConfigs = new ArrayList<Config>(getBuildStepConfigProvider().getAllConfigs());
            Collections.sort(allConfigs, new Comparator<Config>() {
                public int compare(Config o1, Config o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
            return allConfigs;
        }

        /**
         * Returns a Config object for a given config file Id.
         * 
         * @param id
         *            The Id of a config file.
         * @return If Id can be found a Config object that represents the given Id is returned. Otherwise null.
         */
        public ScriptConfig getBuildStepConfigById(String id) {
            return (ScriptConfig) getBuildStepConfigProvider().getConfigById(id);
        }

        /**
         * gets the argument description to be displayed on the screen when selecting a config in the dropdown
         * 
         * @param configId
         *            the config id to get the arguments description for
         * @return the description
         */
        @JavaScriptMethod
        public String getArgsDescription(String configId) {
            final ScriptConfig config = getBuildStepConfigById(configId);
            if (config != null) {
                if (config.args != null && !config.args.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Required arguments: ");
                    int i = 1;
                    for (Iterator<Arg> iterator = config.args.iterator(); iterator.hasNext(); i++) {
                        Arg arg = iterator.next();
                        sb.append(i).append(". ").append(arg.name);
                        if (iterator.hasNext()) {
                            sb.append(" | ");
                        }
                    }
                    return sb.toString();
                } else {
                    return "No arguments required";
                }
            }
            return "please select a script!";
        }

        /**
         * validate that an existing config was chosen
         * 
         * @param value
         *            the configId
         * @return
         */
        public FormValidation doCheckBuildStepId(@QueryParameter String buildStepId) {
            final ScriptConfig config = getBuildStepConfigById(buildStepId);
            if (config != null) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("you must select a valid script");
            }
        }

        private ConfigProvider getBuildStepConfigProvider() {
            ExtensionList<ConfigProvider> providers = ConfigProvider.all();
            return providers.get(WinBatchConfig.WinBatchConfigProvider.class);
        }

        /**
         * Creates a new instance of LibraryBuildStep.
         * 
         * @param req
         *            The web request as initialized by the user.
         * @param json
         *            A JSON object representing the users input.
         * @return A LibraryBuildStep instance.
         */
        @Override
        public WinBatchBuildStep newInstance(StaplerRequest req, JSONObject json) {
            logger.log(Level.FINE, "New instance of LibraryBuildStep requested with JSON data:");
            logger.log(Level.FINE, json.toString(2));

            String id = json.getString("buildStepId");
            final JSONObject definedArgs = json.optJSONObject("defineArgs");
            if (definedArgs != null && !definedArgs.isNullObject()) {
                JSONObject argsObj = definedArgs.optJSONObject("buildStepArgs");
                if (argsObj == null) {
                    JSONArray argsArrayObj = definedArgs.optJSONArray("buildStepArgs");
                    String[] args = null;
                    if (argsArrayObj != null) {
                        Iterator<JSONObject> arguments = argsArrayObj.iterator();
                        args = new String[argsArrayObj.size()];
                        int i = 0;
                        while (arguments.hasNext()) {
                            args[i++] = arguments.next().getString("arg");
                        }
                    }
                    return new WinBatchBuildStep(id, args);
                } else {
                    String[] args = new String[1];
                    args[0] = argsObj.getString("arg");
                    return new WinBatchBuildStep(id, args);
                }
            } else {
                return new WinBatchBuildStep(id, null);
            }
        }
    }

}

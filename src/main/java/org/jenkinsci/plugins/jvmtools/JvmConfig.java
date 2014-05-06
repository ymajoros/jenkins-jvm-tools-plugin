package org.jenkinsci.plugins.jvmtools;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Kohsuke Kawaguchi (original)
 * @author Yannick Majoros
 */
@Extension
public class JvmConfig extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(Descriptor.class.getName());

    private List<JvmConfigItem> jvmConfigItems = new ArrayList<>();

    public JvmConfig() {
        load();
    }

    public List<JvmConfigItem> getJvmConfigItems() {
        return jvmConfigItems;
    }

    public void setJvmConfigItems(List<JvmConfigItem> jvmConfigItems) {
        this.jvmConfigItems = jvmConfigItems;
    }

    @Override
    public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
        staplerRequest.bindJSON(this, json);
        save();

        return true;
    }

    public static JvmConfig get() {
        return GlobalConfiguration.all().get(JvmConfig.class);
    }

    /*
     * form validations...
     *
     */
    public AutoCompletionCandidates doAutoCompleteAssignedLabelString(@QueryParameter String value) {
        AutoCompletionCandidates autoCompletionCandidates = new AutoCompletionCandidates();
        Set<Label> labels = Jenkins.getInstance().getLabels();
        List<String> queries = new AutoCompleteSeeder(value).getSeeds();

        for (String queryTerm : queries) {
            for (Label label : labels) {
                String labelName = label.getName();
                if (labelName.startsWith(queryTerm)) {
                    autoCompletionCandidates.add(label.getName());
                }
            }
        }
        return autoCompletionCandidates;
    }

    /**
     * Utility class for taking the current input value and computing a list of
     * potential terms to match against the list of defined labels.
     */
    static class AutoCompleteSeeder {

        private final String source;

        AutoCompleteSeeder(String source) {
            this.source = source;
        }

        List<String> getSeeds() {
            List<String> terms = new ArrayList<>();
            boolean trailingQuote = source.endsWith("\"");
            boolean leadingQuote = source.startsWith("\"");
            boolean trailingSpace = source.endsWith(" ");

            if (trailingQuote || (trailingSpace && !leadingQuote)) {
                terms.add("");
            } else {
                if (leadingQuote) {
                    int quote = source.lastIndexOf('"');
                    if (quote == 0) {
                        terms.add(source.substring(1));
                    } else {
                        terms.add("");
                    }
                } else {
                    int space = source.lastIndexOf(' ');
                    if (space > -1) {
                        terms.add(source.substring(space + 1));
                    } else {
                        terms.add(source);
                    }
                }
            }

            return terms;
        }
    }
}

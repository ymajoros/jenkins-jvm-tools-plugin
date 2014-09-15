package org.jenkinsci.plugins.jvmtools.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jenkinsci.plugins.jvmtools.JvmConfig;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;

/**
 *
 * @author ym
 */
public class JvmConfigUtil {

    public static JvmConfigItem getBuildStepConfigByName(String jvmConfigName) {
        Collection<JvmConfigItem> availableJvmConfigItems = getAvailableJvmConfigItems();
        for (JvmConfigItem availableJvmConfigItem : availableJvmConfigItems) {
            String availableJvmConfigItemName = availableJvmConfigItem.getName();
            if (availableJvmConfigItemName.equals(jvmConfigName)) {
                return availableJvmConfigItem;
            }
        }
        return null;
    }

    /**
     * Return all jvm configurations that the user can choose from when creating
     * a build step. Ordered by name.
     *
     * @return A collection of jvm configurations of type {@link JvmConfigItem}.
     */
    public static Collection getAvailableJvmConfigItems() {
        JvmConfig jvmConfig = JvmConfig.get();
        List<JvmConfigItem> jvmConfigItems = jvmConfig.getJvmConfigItems();
        Collections.sort(jvmConfigItems, new JvmConfigItemComparator());
        return jvmConfigItems;
    }

}

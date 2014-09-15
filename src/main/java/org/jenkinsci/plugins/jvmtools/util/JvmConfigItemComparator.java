package org.jenkinsci.plugins.jvmtools.util;

import java.util.Comparator;
import org.jenkinsci.plugins.jvmtools.JvmConfigItem;

/**
 *
 * @author ym
 */
public class JvmConfigItemComparator implements Comparator<JvmConfigItem> {

    @Override
    public int compare(JvmConfigItem jvmConfigItem1, JvmConfigItem jvmConfigItem2) {
        String name1 = jvmConfigItem1.getName();
        String name2 = jvmConfigItem2.getName();
        return name1.compareToIgnoreCase(name2);
    }

}

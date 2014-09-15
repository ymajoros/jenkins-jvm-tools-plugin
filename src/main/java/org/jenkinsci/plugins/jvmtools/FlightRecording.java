package org.jenkinsci.plugins.jvmtools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author ym
 */
public class FlightRecording {

    @Nullable
    private final String instanceName;
    @Nonnull
    private final JvmConfigItem jvmConfigItem;
    // can change until written
    @Nullable
    private String fileName;
    // won't be set on creation
    @Nullable
    private String canonicalName;

    public FlightRecording(@Nullable String instanceName, @Nonnull JvmConfigItem jvmConfigItem, @Nullable String fileName) {
        this.instanceName = instanceName;
        this.jvmConfigItem = jvmConfigItem;
        this.fileName = fileName;
    }

    public FlightRecording(@Nonnull String instanceName, @Nonnull JvmConfigItem jvmConfigItem) {
        this(instanceName, jvmConfigItem, null);
    }

    public FlightRecording(@Nonnull JvmConfigItem jvmConfigItem, @Nonnull String fileName) {
        this(null, jvmConfigItem, fileName);
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public JvmConfigItem getJvmConfigItem() {
        return jvmConfigItem;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}

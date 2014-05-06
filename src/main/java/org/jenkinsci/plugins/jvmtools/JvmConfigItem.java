package org.jenkinsci.plugins.jvmtools;

import org.kohsuke.stapler.DataBoundConstructor;


/**
 * Represents a setup config for one set of labels. It may have its own prepare
 * script, files to copy and command line.
 */
public class JvmConfigItem {

    private final String name;
    private final String hostName;
    private final int port;
    private final String userName;
    private final String password;

    //<editor-fold defaultstate="collapsed" desc="get/set...">
    public String getName() {
        return name;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
    //</editor-fold>

    @DataBoundConstructor
    public JvmConfigItem(String name, String hostName, int port, String userName, String password) {
        this.name = name;
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

}

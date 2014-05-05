package org.jenkinsci.plugins.jvmtools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author Yannick Majoros
 */
public class SimpleJMXConnectorFactory {

    private final static String KEY_CREDENTIALS = "jmx.remote.credentials";

    public static JMXConnector createJMXConnector(String host, int port, String user, String password) throws MalformedURLException, IOException {
        Map<String, Object> map = new HashMap<>();
        if (user != null || password != null) {
            String[] credentials = new String[]{user, password};
            map.put(KEY_CREDENTIALS, credentials);
        }
        // Use same convention as Sun. localhost:0 means
        // "VM, monitor thyself!"
        JMXServiceURL jmxServiceURL = createConnectionURL(host, port);
        JMXConnector jmxConnector = JMXConnectorFactory.newJMXConnector(jmxServiceURL, map);
        jmxConnector.connect();
        return jmxConnector;
    }

    private static JMXServiceURL createConnectionURL(String host, int port) throws MalformedURLException {
        String urlPath = "/jndi/rmi://" + host + ":" + port + "/jmxrmi";
        JMXServiceURL jmxServiceURL = new JMXServiceURL("rmi", "", 0, urlPath);
        return jmxServiceURL;
    }
}

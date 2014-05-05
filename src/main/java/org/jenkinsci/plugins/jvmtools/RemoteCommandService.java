package org.jenkinsci.plugins.jvmtools;

import java.io.IOException;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

/**
 * @author Marcus Hirt (original)
 * @author Yannick Majoros
 */
public final class RemoteCommandService {


    private final JMXConnector jmxConnector;

    public RemoteCommandService(JMXConnector jmxConnector) {
        this.jmxConnector = jmxConnector;
    }

    public Object executeCommand(String objectName, String command, String[] signature, Object... args) throws JMException, IOException {
        jmxConnector.connect();
        MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
        ObjectName commandObjectName = new ObjectName(objectName);
        Object commandResult = mBeanServerConnection.invoke(commandObjectName, command, args, signature);
        return commandResult;
    }

    public AttributeList getAttributeList(String name, Object... args) throws JMException, IOException {
        jmxConnector.connect();
        MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
        ObjectName perfCounterObjectName = new ObjectName(name);
        MBeanAttributeInfo[] attributes = mBeanServerConnection.getMBeanInfo(perfCounterObjectName).getAttributes();

        String[] attributeNames = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            attributeNames[i] = attributes[i].getName();
        }
        AttributeList attributeList = mBeanServerConnection.getAttributes(perfCounterObjectName, attributeNames);
        return attributeList;
    }
}

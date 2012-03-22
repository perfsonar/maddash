package net.es.maddash.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class JMXServerAgent {

    public static void premain(String agentArgs)  throws IOException{
        int port= Integer.parseInt(
                System.getProperty("maddash.jmx.port","3000"));
        String host = System.getProperty("maddash.jmx.host",InetAddress.getLocalHost().getHostName());
        LocateRegistry.createRegistry(port);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + host + 
                ":" + port + "/jndi/rmi://" + host + ":" + port + "/server");
        System.out.println("url=" + url);
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url,  new HashMap<String,Object>(), mbs);
        cs.start();
    }    
}

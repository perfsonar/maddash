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

/**
 * Server agent that start Java Monitoring Extensions (JMX) server. Very useful
 * for debugging memory and threading issues. This should be made the Premain class
 * of a JAR that is passed to the <i>-javaagent</i> parameter of the JVM. This 
 * implementation will listen on a single port and generates a url like the following:
 *      service:jmx:rmi://HOSTNAME:PORT/jndi/rmi://HOSTNAME:PORT/server
 * HOSTNAME defaults to the local address (e.g. 127.0.0.1) and port defaults to 300. 
 * They can be set with the System properties maddash.jmx.host and maddash.jmx.port
 * respectively. Currently this implementation does not have any security settings so 
 * use appropriately. An example of a Java command that sets all these properties is below:
 * 
 * java -Xmx256m -Dmaddash.jmx.port=8080 -Dmaddash.jmx.host=localhost -javaagent:target/maddash-server-0.1-jmx.jar -Djava.net.preferIPv4Stack=true -jar target/maddash-server-0.1.one-jar.jar  $*
 * 
 * @author Andy Lake<andy@es.net>
 *
 */
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

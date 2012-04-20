package net.es.maddash;

import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;

/**
 * Main class that starts scheduler and REST server
 * 
 * @author Andy Lake <andy@es.net>
 * 
 */
public class Invoker {

    public static void main(String[] args){
        //Read command line options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("c", "config"), "configuration file").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "log4j"), "log4j configuration file").withRequiredArg().ofType(String.class);
            }
        };
        
        OptionSet opts = parser.parse(args);
        if(opts.has("h")){
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e){}
            System.exit(0);
        }
        
        String configFile = "./etc/maddash.yaml";
        if(opts.has("c")){
            configFile = (String) opts.valueOf("c");
        }
        
        String logConfigFile = "./etc/log4j.properties";
        if(opts.has("l")){
            logConfigFile = (String) opts.valueOf("l");
        }
        System.setProperty("log4j.configuration", "file:" + logConfigFile);
        
        //initialize database and threads
        Logger log = Logger.getLogger(Invoker.class);
        Logger netlogger = Logger.getLogger("netlogger");
        NetLogger netLog = NetLogger.getTlogger();
        MaDDashGlobals globals = null;
        try {
            netlogger.info(netLog.start("maddash.init"));
            MaDDashGlobals.init(configFile);
            globals = MaDDashGlobals.getInstance();
            globals.getWebServer().start();
            netlogger.info(netLog.end("maddash.init"));
        } catch (Exception e) {
            netlogger.error(netLog.error("maddash.init", e.getMessage()));
            System.err.println("Initialization error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        // Block forever
        Object blockMe = new Object();
        synchronized (blockMe) {
            try {
                blockMe.wait();
            } catch (InterruptedException e) {}
        }
    }
}

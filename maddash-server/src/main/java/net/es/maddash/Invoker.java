package net.es.maddash;

import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;

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
            netlogger.info(netLog.end("maddash.init"));
        } catch (Exception e) {
            netlogger.error(netLog.error("maddash.init", e.getMessage()));
            System.err.println("Initialization error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        //start jetty server
        MaDDashHTTPHandler handler = new MaDDashHTTPHandler(globals.getUrlRoot());
        Server server = new Server(globals.getServerPort());
        server.setHandler(handler);
        try {
            log.info("MaDash server started");
            server.start();
            server.join();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}

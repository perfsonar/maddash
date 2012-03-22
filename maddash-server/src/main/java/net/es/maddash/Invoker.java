package net.es.maddash;

import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.mortbay.jetty.Server;

public class Invoker {
    
    
    public static void main(String[] args){
        //Read command line options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("c", "config"), "configuration file").withRequiredArg().ofType(String.class);
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
        
        //initialize database and threads
        MaDDashGlobals globals = null;
        try {
            MaDDashGlobals.init(configFile);
            globals = MaDDashGlobals.getInstance();
        } catch (Exception e) {
            System.err.println("Initialization error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        //start jetty server
        MaDDashHTTPHandler handler = new MaDDashHTTPHandler(globals.getUrlRoot());
        Server server = new Server(globals.getServerPort());
        server.setHandler(handler);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}

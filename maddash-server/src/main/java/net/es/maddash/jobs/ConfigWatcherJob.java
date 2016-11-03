package net.es.maddash.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import org.apache.log4j.Logger;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;

public class ConfigWatcherJob extends Thread{
    Logger log = Logger.getLogger(ConfigWatcherJob.class);
    Logger netlogger = Logger.getLogger("netlogger");
    long DEFAULT_SLEEP_TIME = 10000;//wait 10 seconds in between changes
    WatchService watcher;
    File configFile;

    public ConfigWatcherJob(String name, String configFilename) throws IOException{
        super(name);
        this.watcher = FileSystems.getDefault().newWatchService();
        //get config file parent directory
        this.configFile = new File(configFilename);
        //register directory with watcher
        this.log.debug("config to watch = " + configFilename);
        FileSystems.getDefault().getPath(this.configFile.getParent()).register(watcher, ENTRY_MODIFY);
    }
    
    public void run(){
        while(true){
            try{
                this.execute();
            }catch(Exception e){
                log.error("Error executing ConfigWatcherJob: " + e.getMessage());
            }finally{
                try {
                    Thread.sleep(DEFAULT_SLEEP_TIME);
                } catch (InterruptedException e) {
                    log.error("Interrupt exception: " + e.getMessage());
                }
            }
        }
    }

    private void execute(){
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.debug(netLog.start("maddash.ConfigWatcherJob.execute"));
        WatchKey key = null;
        try {
            key = this.watcher.take();
            for (WatchEvent<?> event: key.pollEvents()) {
                if(this.configFile.getName().equals(event.context().toString())){
                    this.log.debug(this.configFile + " updated, reloading...");
                    MaDDashGlobals.getInstance().reload();
                    this.log.debug("MaDDash reloaded");
                }
            }
            key.reset();
            netlogger.debug(netLog.end("maddash.ConfigWatcherJob.execute"));
        }catch(Exception e){
            if(key != null){
                key.reset();
            }
            netlogger.error(netLog.error("maddash.ConfigWatcherJob.execute", "Error watching config file: " + e.getMessage()));
        }
        
    }
}

package net.es.maddash.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CleanDBJob implements Job{
    Logger log = Logger.getLogger(CheckSchedulerJob.class);
    Logger netlogger = Logger.getLogger("netlogger");
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        NetLogger netLog = NetLogger.getTlogger();
        Connection conn = null;
        try{
            netlogger.info(netLog.start("maddash.CleanDBJob.execute"));
            MaDDashGlobals globals = MaDDashGlobals.getInstance();
            conn = globals.getDataSource().getConnection();
            long oldestAllowedTime = System.currentTimeMillis()/1000L - globals.getDbDataMaxAge();
            this.log.info("oldestAllowedTime=" + oldestAllowedTime);
            
            //clean-up results
            PreparedStatement resultsStmt = conn.prepareStatement("DELETE FROM results WHERE checkTime < ?");
            resultsStmt.setLong(1, oldestAllowedTime);
            resultsStmt.execute();
            
            PreparedStatement checkStmt = conn.prepareStatement("DELETE FROM checks WHERE active=0 AND nextCheckTime < ?");
            checkStmt.setLong(1, oldestAllowedTime);
            checkStmt.execute();
            
            conn.close();
            netlogger.info(netLog.end("maddash.CleanDBJob.execute"));
        }catch(Exception e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            netlogger.debug(netLog.error("maddash.CleanDBJob.execute", e.getMessage()));
            log.error("Error cleaning database " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        
    }

}

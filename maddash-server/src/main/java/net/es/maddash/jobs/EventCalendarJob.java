package net.es.maddash.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import net.es.maddash.checks.CheckConstants;

import org.apache.log4j.Logger;


public class EventCalendarJob extends Thread{
    Logger log = Logger.getLogger(EventCalendarJob.class);
    Logger netlogger = Logger.getLogger("netlogger");
    long DEFAULT_SLEEP_TIME = 60000;
    
    public void run(){
        while(true){
            try{
                this.execute();
            }catch(Exception e){
                log.error("Error executing EventCalendarJob: " + e.getMessage());
            }finally{
                try {
                    Thread.sleep(DEFAULT_SLEEP_TIME);
                } catch (InterruptedException e) {
                    log.error("Interrupt exception: " + e.getMessage());
                }
            }
        }
    }
    
    public void execute(){
        NetLogger netLog = NetLogger.getTlogger();
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        Connection conn = null;
        
        try{
            netlogger.debug(netLog.start("maddash.EventCalendarJob.execute"));
            MaDDashGlobals globals = MaDDashGlobals.getInstance();
            conn = globals.getDataSource().getConnection();
            long now = System.currentTimeMillis()/1000;
            
            //delete all expired events
            PreparedStatement expireEventChecksStmt = conn.prepareStatement("DELETE FROM eventChecks WHERE eventId IN (SELECT id FROM events WHERE endTime <= ? AND endTime > 0)");
            expireEventChecksStmt.setLong(1, now);
            PreparedStatement expireEventsStmt = conn.prepareStatement("DELETE FROM events WHERE endTime <= ? AND endTime > 0");
            expireEventsStmt.setLong(1, now);
            
            //set status on all started events
            PreparedStatement updateChecksStmt = conn.prepareStatement("UPDATE checks SET checkStatus=? WHERE id IN (SELECT DISTINCT checkId FROM eventChecks INNER JOIN events ON eventChecks.eventId = events.id WHERE events.startTime <= ? AND events.changeStatus=1)");
            updateChecksStmt.setInt(1, CheckConstants.RESULT_MAINTENANCE);
            updateChecksStmt.setLong(2, now);
            
            //reset orphaned checks
            PreparedStatement resetChecksStmt = conn.prepareStatement("UPDATE checks SET checkStatus=? WHERE checkStatus=? AND id NOT IN (SELECT DISTINCT checkId FROM eventChecks INNER JOIN events ON eventChecks.eventId = events.id WHERE events.startTime <= ?)");
            resetChecksStmt.setInt(1, CheckConstants.RESULT_NOTRUN);
            resetChecksStmt.setInt(2, CheckConstants.RESULT_MAINTENANCE);
            resetChecksStmt.setLong(3, now);
            
            //run all checks
            netLogParams.put("eventChecksExpired", expireEventChecksStmt.executeUpdate()+"");
            netLogParams.put("eventsExpired", expireEventsStmt.executeUpdate()+"");
            netLogParams.put("checksDowned", updateChecksStmt.executeUpdate()+"");
            netLogParams.put("checksReset", resetChecksStmt.executeUpdate()+"");
            //System.out.println(netLog.end("maddash.EventCalendarJob.execute", null, null, netLogParams));
            netlogger.debug(netLog.end("maddash.EventCalendarJob.execute", null, null, netLogParams));
        }catch(Exception e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            netlogger.debug(netLog.error("maddash.EventCalendarJob.execute", e.getMessage()));
            log.error("Error cleaning database " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        
    }
}

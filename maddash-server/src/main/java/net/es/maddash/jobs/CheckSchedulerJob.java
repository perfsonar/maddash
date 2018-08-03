package net.es.maddash.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import net.es.maddash.checks.CheckConstants;
import net.es.maddash.utils.DimensionUtil;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import static org.quartz.TriggerBuilder.*;
import static org.quartz.JobBuilder.*;

/**
 * Queries database for checks that need to be run. It will only pull a 
 * maximum number of rows as specified for the global jobBatchSize. This
 * prevents the queue from infinitely growing. It also limits how many 
 * jobs can be run every time this job runs. For example if jobBatchSize 
 * is 250 and this is run every minute, then the most jobs it will run is 
 * 250 per minute.If we did not have this limit you'd likely run into OutOfMemory
 * errors over time if you have lots of jobs to run that are not very quickly.
 * 
 * @author Andy Lake <andy@es.net>
 *
 */
public class CheckSchedulerJob extends Thread{
    Logger log = Logger.getLogger(CheckSchedulerJob.class);
    Logger netlogger = Logger.getLogger("netlogger");
    long DEFAULT_SLEEP_TIME = 20000;
    
    public CheckSchedulerJob(String name){
        super(name);
    }
    
    public void run(){
        while(true){
            try{
                this.execute();
            }catch(Exception e){
                log.error("Error executing CheckSchedulerJob: " + e.getMessage());
            }finally{
                try {
                    Thread.sleep(DEFAULT_SLEEP_TIME);
                } catch (InterruptedException e) {
                    log.error("Interrupt exception: " + e.getMessage());
                }
            }
        }
    }
    
    synchronized public void execute() {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.CheckSchedulerJob.execute"));
        int schedJobCount = 0;
        int totalJobCount = 0;
        
        //query database
        Connection conn = null;
        try{
            /* garbage collect before hitting database. memory spikes occur 
             * here as we pull in new check data so this maximizes memory 
             * available prior to spike and should help prevent heap errors */
            System.gc();
            
            MaDDashGlobals globals = MaDDashGlobals.getInstance();
            conn = globals.getDataSource().getConnection();
            long time = System.currentTimeMillis()/1000;
            netlogger.debug(netLog.start("maddash.CheckSchedulerJob.execute.queryDb"));
            PreparedStatement selStmt = conn.prepareStatement("SELECT c.id, c.gridName, " +
                "c.rowName, c.colName, t.checkType, t.checkParams, t.checkInterval, " +
                "t.retryInterval, t.retryAttempts, t.timeout, c.statusMessage FROM checkTemplates AS t, " +
                "checks AS c WHERE c.active = 1 AND t.id = c.checkTemplateId AND " +
                "c.nextCheckTime <= ? AND c.checkStatus != ? ORDER BY c.nextCheckTime ASC FETCH FIRST ? ROWS ONLY");
            selStmt.setLong(1, time);
            selStmt.setInt(2, CheckConstants.RESULT_MAINTENANCE);
            selStmt.setInt(3, globals.getJobBatchSize());
            //Below is redundant with above and may cause ArrayIndexOutofBounds errors
            //selStmt.setMaxRows(globals.getJobBatchSize());
            ResultSet checksToRun = selStmt.executeQuery();
            netlogger.debug(netLog.end("maddash.CheckSchedulerJob.execute.queryDb"));
            
            while(checksToRun.next()){
                totalJobCount++;
                if(globals.isCheckScheduled(checksToRun.getInt(1))){
                    continue;
                }
                String jobKey =  UUID.randomUUID().toString();
                String triggerName = "runCheckTrigger-" + jobKey;
                String jobName = "runCheckJob-" + jobKey;
                Trigger trigger = newTrigger()
                        .withIdentity(triggerName)
                        .startNow()
                        .build();
                JobDataMap dataMap = new JobDataMap();
                dataMap.put("checkId", checksToRun.getInt(1));
                dataMap.put("gridName", checksToRun.getString(2));
                dataMap.put("rowName", checksToRun.getString(3));
                dataMap.put("colName", checksToRun.getString(4));
                dataMap.put("checkType", globals.getCheckTypeClassMap().get(checksToRun.getString(5)));
                dataMap.put("checkParams", checksToRun.getString(6));
                dataMap.put("checkInterval", checksToRun.getInt(7));
                dataMap.put("retryInterval", checksToRun.getInt(8));
                dataMap.put("retryAttempts", checksToRun.getInt(9));
                dataMap.put("timeout", checksToRun.getInt(10));
                dataMap.put("statusMessage", checksToRun.getString(11));
                dataMap.put("rowVars", DimensionUtil.getParams(checksToRun.getString(3), checksToRun.getString(4), conn));
                dataMap.put("colVars", DimensionUtil.getParams(checksToRun.getString(4), checksToRun.getString(3),conn));
                JobDetail jobDetail = newJob(RunCheckJob.class)
                        .withIdentity(jobName, "RUN_CHECKS")
                        .usingJobData(dataMap)
                        .build();
                globals.updateScheduledChecks(checksToRun.getInt(1), true);
                globals.getScheduler().scheduleJob(jobDetail, trigger);
                schedJobCount++;
            }
            conn.close();
            netlogger.info(netLog.end("maddash.CheckSchedulerJob.execute"));
            log.debug("Scheduled " + schedJobCount + "/" + totalJobCount + " new jobs");
        }catch(SchedulerException e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            String msg = "The scheduler threw an exception. This often happens during configuration reloading and can be ignored. Exact error is: " + e.getMessage();
            netlogger.warn(netLog.error("maddash.CheckSchedulerJob.execute", msg));
            log.warn(msg);
        }catch(Exception e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            netlogger.info(netLog.error("maddash.CheckSchedulerJob.execute", e.getMessage()));
            log.error("Error scheduling job " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        
    }

}

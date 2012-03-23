package net.es.maddash.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;

public class CheckSchedulerJob implements Job{
    Logger log = Logger.getLogger(CheckSchedulerJob.class);
    Logger netlogger = Logger.getLogger("netlogger");
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        NetLogger netLog = NetLogger.getTlogger();
        
        //query database
        Connection conn = null;
        try{
            netlogger.info(netLog.start("maddash.CheckSchedulerJob.execute"));
            MaDDashGlobals globals = MaDDashGlobals.getInstance();
            conn = globals.getDataSource().getConnection();
            PreparedStatement selStmt = conn.prepareStatement("SELECT c.id, c.gridName, " +
                "c.rowName, c.colName, t.checkType, t.checkParams, t.checkInterval, " +
                "t.retryInterval, t.retryAttempts, t.timeout FROM checkTemplates AS t, " +
                "checks AS c WHERE c.active = 1 AND t.id = c.checkTemplateId AND " +
                "c.nextCheckTime <= ? ORDER BY c.nextCheckTime ASC");
            long time = System.currentTimeMillis()/1000;
            selStmt.setLong(1, time);
            selStmt.setMaxRows(globals.getJobBatchSize());
            ResultSet checksToRun = selStmt.executeQuery();
            int jobCount = 0;
            synchronized(CheckSchedulerJob.class){
                while(checksToRun.next()){
                    if(globals.isCheckScheduled(checksToRun.getInt(1))){
                        continue;
                    }
                    String jobKey =  UUID.randomUUID().toString();
                    String triggerName = "runCheckTrigger-" + jobKey;
                    String jobName = "runCheckJob-" + jobKey;
                    SimpleTrigger trigger = new SimpleTrigger(triggerName, null, 
                            new Date(), null, 0, 0L);
                    JobDetail jobDetail = new JobDetail(jobName, "RUN_CHECKS",
                            RunCheckJob.class);
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
                    jobDetail.setJobDataMap(dataMap);
                    globals.updateScheduledChecks(checksToRun.getInt(1), true);
                    globals.getScheduler().scheduleJob(jobDetail, trigger);
                    jobCount++;
                }
            }
            conn.close();
            netlogger.info(netLog.end("maddash.CheckSchedulerJob.execute"));
            log.debug("Scheduled " + jobCount + " new jobs");
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

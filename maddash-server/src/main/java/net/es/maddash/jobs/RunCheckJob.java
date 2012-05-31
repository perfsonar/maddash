package net.es.maddash.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import net.es.maddash.checks.Check;
import net.es.maddash.checks.CheckConstants;
import net.es.maddash.checks.CheckResult;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job that runs a specified check and adds the results to the database
 * 
 * @author Andy Lake<andy@es.net>
 *
 */
public class RunCheckJob implements Job{
    private Logger log = Logger.getLogger(CheckSchedulerJob.class);
    private Logger netlogger = Logger.getLogger("netlogger");
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.debug(netLog.start("maddash.RunCheckJob.execute"));
        //load jobdatamap
        MaDDashGlobals globals =  null;
        try{
            globals = MaDDashGlobals.getInstance();
        }catch(Exception e){
            log.error("Error loading global: " + e.getMessage());
            e.printStackTrace();
        }
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        int checkId = dataMap.getInt("checkId");
        String gridName = dataMap.getString("gridName");
        String rowName = dataMap.getString("rowName");
        String colName = dataMap.getString("colName");
        Class checkClass = (Class) dataMap.get("checkType");
        String checkParams = dataMap.getString("checkParams");
        int timeout = dataMap.getInt("timeout");
        
        //load check
        JSONObject paramJson = null;
        Check checkToRun = null;
        try {
            if(checkParams != null && !CheckConstants.EMPTY_PARAMS.equals(checkParams)){
                paramJson = JSONObject.fromObject(checkParams);
            }
            checkToRun = (Check)checkClass.newInstance();
        } catch (Exception e) {
            this.deactivateCheck(checkId, globals);
            log.error("Error loading check: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        //run check
        HashMap<String,String> netLogFields = new HashMap<String,String>();
        netLogFields.put("grid", gridName);
        netLogFields.put("row", rowName);
        netLogFields.put("col", colName);
        
        CheckResult result = null;
        try{
            netlogger.info(netLog.start("maddash.RunCheckJob.execute.runCheck", null, null, netLogFields));
            result = checkToRun.check(gridName, rowName, colName, paramJson, timeout);
            netLogFields.put("resultCode", result.getResultCode()+"");
            netLogFields.put("resultMsg", result.getMessage());
            netlogger.info(netLog.end("maddash.RunCheckJob.execute.runCheck", null, null, netLogFields));
            log.debug("Result code is " + result.getResultCode());
            log.debug("Result msg is " + result.getMessage());
        }catch(Exception e){
            result = new CheckResult(CheckConstants.RESULT_UNKNOWN, e.getMessage(), null);
            netlogger.error(netLog.end("maddash.RunCheckJob.execute.runCheck", e.getMessage(), null, netLogFields));
            log.error("Error running check: " + e.getMessage());
            e.printStackTrace();
        }
        
        Connection conn = null;
        try {
            netlogger.debug(netLog.start("maddash.RunCheckJob.execute.updateDatabase"));
            conn = globals.getDataSource().getConnection();
            this.updateDatabase(result, dataMap, conn);
            conn.close();   
            netlogger.debug(netLog.end("maddash.RunCheckJob.execute.updateDatabase"));
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.debug(netLog.error("maddash.RunCheckJob.execute.updateDatabase", e.getMessage()));
            e.printStackTrace();
        }
        
        //unschedule check
        globals.updateScheduledChecks(checkId, false);
        netlogger.debug(netLog.end("maddash.RunCheckJob.execute"));
    }

    public void updateDatabase(CheckResult result, JobDataMap dataMap, Connection conn) throws SQLException{
        NetLogger netLog = NetLogger.getTlogger();
        int checkId = dataMap.getInt("checkId");
        String gridName = dataMap.getString("gridName");
        String rowName = dataMap.getString("rowName");
        String colName = dataMap.getString("colName");
        int checkInterval = dataMap.getInt("checkInterval");
        int retryInterval = dataMap.getInt("retryInterval");
        int retryAttempts = dataMap.getInt("retryAttempts");
        String lastCheckMessage = dataMap.getString("statusMessage");
        
        //find last result
        int lastReturnCode = result.getResultCode();
        int lastResultCount = 0;
        int lastCheckStatus = result.getResultCode();
        
        netlogger.debug(netLog.start("maddash.RunCheckJob.execute.updateDatabase.select"));
        PreparedStatement selStmt = conn.prepareStatement("SELECT returnCode, resultCount, checkStatus FROM results WHERE checkId=? AND " +
                "checkTime=(SELECT MAX(checkTime) FROM results WHERE checkId=?)");
        selStmt.setInt(1, checkId);
        selStmt.setInt(2, checkId);
        ResultSet lastCheck = selStmt.executeQuery();
        netlogger.debug(netLog.end("maddash.RunCheckJob.execute.updateDatabase.select"));
        
        if(lastCheck.next()){
            lastReturnCode = lastCheck.getInt(1);
            lastResultCount = lastCheck.getInt(2);
            lastCheckStatus = lastCheck.getInt(3);
        }
        
        //determine next time and final status
        long time = (System.currentTimeMillis()/1000);
        long nextTime = time + checkInterval;
        int newResultCount = 0;
        int finalStatus = result.getResultCode();
        String finalStatusMessage = result.getMessage();
        if(result.getResultCode() != lastCheckStatus){
            newResultCount = 1;
            if(lastReturnCode != lastCheckStatus){
                newResultCount += lastResultCount;
            }
            if(newResultCount < retryAttempts){
                finalStatus = lastCheckStatus;
                finalStatusMessage = lastCheckMessage;
                nextTime = time + retryInterval;
            }
        }
        
        //insert and update database
        String statsString = null;
        if(result.getStats() != null){
            JSONObject tmpJson = JSONObject.fromObject(result.getStats());
            statsString = tmpJson.toString();
        }
        
        netlogger.debug(netLog.start("maddash.RunCheckJob.execute.updateDatabase.insert"));
        PreparedStatement insertResStmt = conn.prepareStatement("INSERT INTO results VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?)");
        insertResStmt.setInt(1, checkId);
        insertResStmt.setLong(2, time);
        insertResStmt.setInt(3, result.getResultCode());
        insertResStmt.setString(4, result.getMessage());
        insertResStmt.setString(5, statsString);
        insertResStmt.setInt(6, newResultCount);
        insertResStmt.setInt(7, finalStatus);
        insertResStmt.executeUpdate();
        netlogger.debug(netLog.end("maddash.RunCheckJob.execute.updateDatabase.insert"));
        
        netlogger.debug(netLog.start("maddash.RunCheckJob.execute.updateDatabase.update"));
        PreparedStatement updateCheckStmt = conn.prepareStatement("UPDATE checks SET prevCheckTime=?, nextCheckTime=?, checkStatus=?, prevResultCode=?, statusMessage=?, resultCount=? WHERE id=?");
        updateCheckStmt.setLong(1, time);
        updateCheckStmt.setLong(2, nextTime);
        updateCheckStmt.setInt(3, finalStatus);
        updateCheckStmt.setInt(4, result.getResultCode());
        updateCheckStmt.setString(5, finalStatusMessage);
        updateCheckStmt.setInt(6, newResultCount);
        updateCheckStmt.setInt(7, checkId);
        updateCheckStmt.executeUpdate();
        netlogger.debug(netLog.end("maddash.RunCheckJob.execute.updateDatabase.update"));
        log.debug("Next run of " + gridName + "." + rowName + "." + colName  + " is " + new Date(nextTime*1000));
    }
    
    private void deactivateCheck(int checkId, MaDDashGlobals globals) {
        Connection conn = null;
        try {
            conn = globals.getDataSource().getConnection();
            PreparedStatement updateCheckStmt = conn.prepareStatement("UPDATE checks SET active=0 WHERE id=?");
            updateCheckStmt.setInt(1, checkId);
            updateCheckStmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            e.printStackTrace();
        }
        globals.updateScheduledChecks(checkId, false);
    }
}

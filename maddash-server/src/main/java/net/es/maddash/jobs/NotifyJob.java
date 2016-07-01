package net.es.maddash.jobs;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.es.maddash.DBMesh;
import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import net.es.maddash.madalert.Madalert;
import net.es.maddash.madalert.Mesh;
import net.es.maddash.madalert.Problem;
import net.es.maddash.madalert.Report;
import net.es.maddash.notifications.Notification;
import net.es.maddash.notifications.NotificationFactory;
import net.es.maddash.notifications.NotifyProblem;
import net.es.maddash.notifications.NotifyProblemComparator;

public class NotifyJob implements Job{
    private Logger log = Logger.getLogger(NotifyJob.class);
    private Logger netlogger = Logger.getLogger("netlogger");
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.NotifyJob.execute"));
        
        //load data map
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        int notificationId = dataMap.getInt("notificationId");
        int minSeverity = dataMap.getInt("minSeverity");
        int frequency = dataMap.getInt("frequency");
        HashMap<String, Boolean> dashboardFilters = (HashMap<String, Boolean>)dataMap.get("dashboardFilters");
        HashMap<String, Boolean> gridFilters = (HashMap<String, Boolean>)dataMap.get("gridFilters");
        HashMap<String, Boolean> siteFilters = (HashMap<String, Boolean>)dataMap.get("siteFilters");
        HashMap<String, Boolean> categoryFilters = (HashMap<String, Boolean>)dataMap.get("categoryFilters");
        
        //query database
        Connection conn = null;
        try{
            //init db and find notification row
            MaDDashGlobals globals = MaDDashGlobals.getInstance();
            conn = globals.getDataSource().getConnection();
            PreparedStatement selStmt = conn.prepareStatement("SELECT name, type, params FROM notifications WHERE id=?");
            selStmt.setInt(1, notificationId);
            ResultSet notificationResult = selStmt.executeQuery();
            if(!notificationResult.next()){
                throw new RuntimeException("Unable to find notification with ID " + notificationId + " in database");
            }
            String name = notificationResult.getString(1);
            String type = notificationResult.getString(2);
            JsonObject params = Json.createReader(new StringReader(notificationResult.getString(3))).readObject();
            
            //find reports we care about
            List<NotifyProblem> problems = new ArrayList<NotifyProblem>();
            List<NotifyProblem> newProblems = new ArrayList<NotifyProblem>();
            HashMap<String, Boolean> gridMap = new HashMap<String, Boolean>();
            //apply dashboard filters
            if(!dashboardFilters.isEmpty()){
                JsonArray dashboards = MaDDashGlobals.getInstance().getDashboards();
                for(int i = 0; i < dashboards.size(); i++){
                    String dashName = dashboards.getJsonObject(i).getString("name");
                    if(dashboardFilters.containsKey(dashName) && dashboardFilters.get(dashName)){
                        JsonArray dashGrids = dashboards.getJsonObject(i).getJsonArray("grids");
                        for(int j = 0; j < dashGrids.size(); j++){
                            String gridName = dashGrids.getJsonObject(j).getString("name").trim();
                            if(gridFilters.isEmpty()){
                                //if no grid filters, assume all should be added
                                gridMap.put(gridName, true);
                            }else if(gridFilters.containsKey(gridName) && gridFilters.get(gridName)){
                                //otherwise, only add if in grid filter list 
                                gridMap.put(gridName, true);
                            }
                        }
                    }
                }
            }else{
                //apply grid filters
                ResultSet gridSel = conn.createStatement().executeQuery("SELECT DISTINCT gridName FROM grids");
                while(gridSel.next()){
                    if(gridFilters.isEmpty()){
                        //if no grid filters, assume all should be added
                        gridMap.put(gridSel.getString(1), true);
                    }else if(gridFilters.containsKey(gridSel.getString(1)) && gridFilters.get(gridSel.getString(1))){
                        //otherwise, only add if in grid filter list 
                        gridMap.put(gridSel.getString(1), true);
                    }
                }
            }
            //generate reports
            for(String gridName : gridMap.keySet()){
                Mesh mesh = new DBMesh(gridName, "");
                Report report = Madalert.lookupRule(mesh.getName()).createReport(mesh);
                //check global problem. if site filters, ignore global
                if(siteFilters.isEmpty()){
                    if(report.getGlobalMaxSeverity() >= minSeverity){
                        if(report.getGlobalProblems() != null){
                            for(Problem p: report.getGlobalProblems()){
                                if(!categoryFilters.isEmpty() && (!categoryFilters.containsKey(p.getCategory()) || categoryFilters.get(p.getCategory()) == null)){
                                    continue;
                                }
                                if(p.getSeverity() >= minSeverity){
                                    problems.add(new NotifyProblem(gridName, p));
                                }
                            }
                        }
                    }
                }
                
                //check site problems
                for(String site : report.getSites()){
                    //skip if site filters defined and this is not our site
                    if(!siteFilters.isEmpty() && !siteFilters.containsKey(site) && !siteFilters.get(site)){
                        continue;
                    }
                    if(report.getSiteProblems(site) != null){
                        for(Problem p: report.getSiteProblems(site)){
                            if(!categoryFilters.isEmpty() && (!categoryFilters.containsKey(p.getCategory()) || categoryFilters.get(p.getCategory()) == null)){
                                continue;
                            }
                            if(p.getSeverity() >= minSeverity){
                                problems.add(new NotifyProblem(gridName, site, p));
                            }
                        }
                    }
                }
            }
            
            //update db and determine if needs to be sent
            long now = System.currentTimeMillis()/1000;
            long expires = now + frequency;
            PreparedStatement probSel = conn.prepareStatement("SELECT id, expires FROM notificationProblems WHERE notificationId=? AND checksum=?");
            PreparedStatement probDelete = conn.prepareStatement("DELETE FROM notificationProblems WHERE expires <= ?");
            PreparedStatement probInsert = conn.prepareStatement("INSERT INTO notificationProblems VALUES(DEFAULT, ?, ?, ?)");
            
            //clean out expired problems (regardless of notification)
            probDelete.setLong(1, now);
            probDelete.executeUpdate();
            //now check if a problem has been reported on recently
            for(NotifyProblem p : problems){
                probSel.setInt(1, notificationId);
                probSel.setString(2, p.checksum());
                ResultSet probResult = probSel.executeQuery();
                //if not in DB, then include, otherwise ignore
                if(!probResult.next()){
                    if(frequency > 0){
                        probInsert.setInt(1, notificationId);
                        probInsert.setString(2, p.checksum());
                        probInsert.setLong(3, expires);
                        probInsert.executeUpdate();
                    }
                    newProblems.add(p);
                }
            }
            
            //create notifier and send reports
            Notification notifier = NotificationFactory.create(name, type, params);
            Collections.sort(newProblems, new NotifyProblemComparator());
            notifier.send(newProblems);
            netlogger.info(netLog.end("maddash.NotifyJob.execute"));
        }catch(Exception e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            netlogger.info(netLog.error("maddash.NotifyJob.execute", e.getMessage()));
            log.error("Error scheduling job " + e.getMessage());
            e.printStackTrace();
        }
    }

}

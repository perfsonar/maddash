package net.es.maddash;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.sql.rowset.serial.SerialClob;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import net.es.maddash.checks.Check;
import net.es.maddash.checks.CheckConstants;
import net.es.maddash.jobs.NotifyJob;
import net.es.maddash.madalert.Madalert;
import net.es.maddash.madalert.Problem;
import net.es.maddash.madalert.Rule;
import net.es.maddash.madalert.SiteRule;
import net.es.maddash.madalert.SiteTestSet;
import net.es.maddash.madalert.StatusMatcher;
import net.es.maddash.madalert.TestSet;
import net.es.maddash.notifications.Notification;
import net.es.maddash.notifications.NotificationFactory;
import net.es.maddash.utils.DimensionUtil;
import net.sf.json.JSONObject;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Loads YAML configuration file into scheduler database. 
 * It will add new checks and disable checks that have been removed.
 * 
 * @author Andy Lake <andy@es.net>
 *
 */
public class ConfigLoader {
    static final Logger log = Logger.getLogger(ConfigLoader.class);

    //properties
    static final public String PROP_DIMENSIONS = "groupMembers";
    static final public String PROP_DIMENSIONS_ID = "id";
    static final public String PROP_DIMENSIONS_LABEL = "label";
    static final public String PROP_DIMENSION_MAP = "map";
    static final public String PROP_GROUPS = "groups";

    static final public String PROP_CHECKS = "checks";
    static final public String PROP_CHECKS_NAME = "name";
    static final public String PROP_CHECKS_DESCRIPTION = "description";
    static final public String PROP_CHECKS_TYPE = "type";
    static final public String PROP_CHECKS_PARAMS = "params";
    static final public String PROP_CHECKS_INTERVAL = "checkInterval";
    static final public String PROP_CHECKS_RETRY_INT = "retryInterval";
    static final public String PROP_CHECKS_RETRY_ATT = "retryAttempts";
    static final public String PROP_CHECKS_TIMEOUT = "timeout";

    static final public String PROP_GRIDS = "grids";
    static final public String PROP_GRIDS_NAME = "name";
    static final public String PROP_GRIDS_ROWS = "rows";
    static final public String PROP_GRIDS_COLS = "columns";
    static final public String PROP_GRIDS_CHECKS = "checks";
    static final public String PROP_GRIDS_EXCL_SELF = "excludeSelf";
    static final public String PROP_GRIDS_EXCL_CHECKS = "excludeChecks";
    static final public String PROP_GRIDS_COL_ALG = "columnAlgorithm";
    static final public String PROP_GRIDS_ROW_ORDER = "rowOrder";
    static final public String PROP_GRIDS_COL_ORDER = "colOrder";
    static final public String PROP_GRIDS_REPORT = "report";
    static final public String PROP_GRIDS_STATUS_LABELS = "statusLabels";
    static final public String PROP_GRIDS_STATUS_LABELS_OK = "ok";
    static final public String PROP_GRIDS_STATUS_LABELS_WARNING = "warning";
    static final public String PROP_GRIDS_STATUS_LABELS_CRITICAL = "critical";
    static final public String PROP_GRIDS_STATUS_LABELS_UNKNOWN = "unknown";
    static final public String PROP_GRIDS_STATUS_LABELS_NOTRUN = "notrun";
    static final public String PROP_GRIDS_STATUS_LABELS_EXTRA = "extra";
    static final public String PROP_GRIDS_STATUS_LABELS_EXTRA_VALUE = "value";
    static final public String PROP_GRIDS_STATUS_LABELS_EXTRA_SHORT_NAME = "shortName";
    static final public String PROP_GRIDS_STATUS_LABELS_EXTRA_DESCR = "description";
    
    static final public String ALG_AFTER = "afterself";
    static final public String ALG_BEFORE = "beforeself";
    static final public String ALG_ALL = "all";
    static final public String ORDER_ALPHA = "alphabetical";
    static final public String ORDER_GROUP = "group";
    static final public String EXCL_CHECKS_DEFAULT = "default";
    static final public String EXCL_CHECKS_ALL = "all";
    
    static final public String PROP_DEFAULT_REPORT = "defaultReport";
    static final public String PROP_REPORTS = "reports";
    static final public String PROP_REPORTS_ID = "id";
    static final public String PROP_REPORTS_RULE = "rule";
    static final public String PROP_REPORTS_RULES = "rules";
    static final public String PROP_REPORTS_RULE_TYPE = "type";
    static final public String PROP_REPORTS_RULE_TYPE_MATCH_FIRST = "matchFirst";
    static final public String PROP_REPORTS_RULE_TYPE_MATCH_ALL = "matchAll";
    static final public String PROP_REPORTS_RULE_TYPE_FOREACH_SITE = "forEachSite";
    static final public String PROP_REPORTS_RULE_TYPE_RULE = "rule";
    static final public String PROP_REPORTS_RULE_TYPE_SITERULE = "siteRule";
    static final public String PROP_REPORTS_RULE_SITE = "site";
    static final public String PROP_REPORTS_RULE_SELECTOR = "selector";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE = "type";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_GRID = "grid";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_SITE = "site";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_ROW = "row";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_COLUMN = "column";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_CELL = "column";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK = "check";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_ROWSITE = "rowSite";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_COLSITE = "colSite";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX = "rowIndex";
    static final public String PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX = "colIndex";
    static final public String PROP_REPORTS_RULE_MATCH = "match";
    static final public String PROP_REPORTS_RULE_MATCH_TYPE = "type";
    static final public String PROP_REPORTS_RULE_MATCH_TYPE_STATUS = "status";
    static final public String PROP_REPORTS_RULE_MATCH_TYPE_STATUS_STATUS = "status";
    static final public String PROP_REPORTS_RULE_MATCH_TYPE_STATUSTHRESH = "statusThreshold";
    static final public String PROP_REPORTS_RULE_MATCH_TYPE_STATUSTHRESH_THRESH = "threshold";
    static final public String PROP_REPORTS_RULE_MATCH_TYPE_STATUSWEIGHTTHRESH = "statusWeightedThreshold";
    static final public String PROP_REPORTS_RULE_MATCH_TYPE_STATUSWEIGHTTHRESH_STATUSES = "statuses";
    static final public String PROP_REPORTS_RULE_PROBLEM = "problem";
    static final public String PROP_REPORTS_RULE_PROBLEM_SEVERITY = "severity";
    static final public String PROP_REPORTS_RULE_PROBLEM_CATEGORY = "category";
    static final public String PROP_REPORTS_RULE_PROBLEM_MSG = "message";
    static final public String PROP_REPORTS_RULE_PROBLEM_SOLUTIONS = "solutions";
    
    static final public String PROP_NOTIFICATIONS = "notifications";
    static final public String PROP_NOTIFICATIONS_NAME = "name";
    static final public String PROP_NOTIFICATIONS_TYPE = "type";
    static final public String PROP_NOTIFICATIONS_SCHEDULE = "schedule";
    static final public String PROP_NOTIFICATIONS_PROBFREQ = "problemReportFrequency";
    static final public String PROP_NOTIFICATIONS_PROBRESOLV = "problemResolveAfter";
    static final public String PROP_NOTIFICATIONS_MINSEV = "minimumSeverity";
    static final public String PROP_NOTIFICATIONS_FILTERS = "filters";
    static final public String PROP_NOTIFICATIONS_FILTERS_TYPE = "type";
    static final public String PROP_NOTIFICATIONS_FILTERS_TYPE_DASHBOARD = "dashboard";
    static final public String PROP_NOTIFICATIONS_FILTERS_TYPE_GRID = "grid";
    static final public String PROP_NOTIFICATIONS_FILTERS_TYPE_SITE = "site";
    static final public String PROP_NOTIFICATIONS_FILTERS_TYPE_CATEGORY = "category";
    static final public String PROP_NOTIFICATIONS_FILTERS_VALUE = "value";
    static final public String PROP_NOTIFICATIONS_PARAMS = "parameters";
    
    /**
     * Loads YAML properties into scheduler database
     * 
     * @param config Properties loaded from YAML file as a Map
     * @param dataSource the dataSource to use to access the database
     * @return a map of check Classes indexed by the class name
     * @throws ClassNotFoundException
     */
    static public Map<String,Class> load(Map config, ComboPooledDataSource dataSource) throws ClassNotFoundException {
        checkRequiredProp(config, PROP_CHECKS);
        checkRequiredProp(config, PROP_GROUPS);
        checkRequiredProp(config, PROP_GRIDS);
        Map<String, Map> checkMap = (Map<String, Map>) config.get(PROP_CHECKS);
        Map groupMap = (Map) config.get(PROP_GROUPS);
        List<Map> gridList = (List<Map>) config.get(PROP_GRIDS);
        HashMap<String, Class> checkTypeClassMap = new HashMap<String, Class>();
        HashMap<String,String> dimensionLabelMap = new HashMap<String,String>();
        HashMap<String,JSONObject> dimensionMap = new HashMap<String,JSONObject>();
        
        Connection conn = null;
        try{
            conn = dataSource.getConnection();

            //populate the dimensions table
            conn.createStatement().executeUpdate("DELETE FROM dimensions");
            PreparedStatement insertDimension = conn.prepareStatement("INSERT INTO dimensions VALUES(DEFAULT, ?, ?, ?)");
            if(config.containsKey(PROP_DIMENSIONS) && config.get(PROP_DIMENSIONS) != null){
                int i = 1;
                for(Map<Object,Object> dimension : (List<Map<Object,Object>>) config.get(PROP_DIMENSIONS)){
                    if(!dimension.containsKey(PROP_DIMENSIONS_ID) || dimension.get(PROP_DIMENSIONS_ID) == null){
                        throw new RuntimeException("Found dimension at position " + i + 
                                " that is missing 'id' attribute");
                    }
                    dimensionMap.put(dimension.get(PROP_DIMENSIONS_ID)+"", new JSONObject());
                    for(Object dimensionParamObj : dimension.keySet()){
                        String dimensionParam = dimensionParamObj+"";
                        String dimensionValue = "";
                        if(dimensionParam.equals(PROP_DIMENSIONS_ID)){
                            continue;
                        }
                        
                        if(dimensionParam.equals(PROP_DIMENSIONS_LABEL)){
                            dimensionLabelMap.put(dimension.get(PROP_DIMENSIONS_ID)+"", dimension.get(dimensionParam)+"");
                        }
                        
                        if(dimensionParam.equals(PROP_DIMENSION_MAP)){
                            JSONObject jsonMap = JSONObject.fromObject(dimension.get(dimensionParam));
                            dimensionValue = jsonMap.toString();
                            dimensionMap.get(dimension.get(PROP_DIMENSIONS_ID)+"").put(dimensionParam, jsonMap);
                        }else{
                            dimensionValue = dimension.get(dimensionParam)+"";
                            dimensionMap.get(dimension.get(PROP_DIMENSIONS_ID)+"").put(dimensionParam, dimensionValue);
                        }
                        
                        insertDimension.setString(1, dimension.get(PROP_DIMENSIONS_ID)+"");
                        insertDimension.setString(2, dimensionParam);
                        SerialClob valueClob = new SerialClob(dimensionValue.toCharArray());
                        insertDimension.setClob(3, valueClob);
                        insertDimension.executeUpdate();
                    }
                    i++;
                }
            }

            //Build prepared statements for each check
            HashMap<String, Integer> templateIdMap = new HashMap<String, Integer>();
            PreparedStatement selTemplateStmt = conn.prepareStatement("SELECT id FROM " +
                    "checkTemplates WHERE templateName=? AND checkType=? AND " +
                    "checkInterval=? AND retryInterval=? AND retryAttempts=? AND timeout=?");
            PreparedStatement insertTemplateStmt = conn.prepareStatement("INSERT INTO " +
                    "checkTemplates VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement updateTemplateStmt = conn.prepareStatement("UPDATE checkTemplates SET checkParams=? WHERE id=?");
            for(String checkName : checkMap.keySet()){
                selTemplateStmt.setString(1, checkName);
                insertTemplateStmt.setString(1, checkName);

                Map check = checkMap.get(checkName);
                checkRequiredProp(check, PROP_CHECKS_NAME);
                checkRequiredProp(check, PROP_CHECKS_DESCRIPTION);
                checkRequiredProp(check, PROP_CHECKS_TYPE);
                if(!checkTypeClassMap.containsKey(check.get(PROP_CHECKS_TYPE)+"")){
                    checkTypeClassMap.put(check.get(PROP_CHECKS_TYPE)+"", 
                            loadClass(check.get(PROP_CHECKS_TYPE)+""));
                }
                selTemplateStmt.setString(2, (String) check.get(PROP_CHECKS_TYPE));
                insertTemplateStmt.setString(2, (String) check.get(PROP_CHECKS_TYPE));

                String jsonParamString = CheckConstants.EMPTY_PARAMS;
                if(check.containsKey(PROP_CHECKS_PARAMS) && check.get(PROP_CHECKS_PARAMS) != null){
                    jsonParamString = JSONObject.fromObject(check.get(PROP_CHECKS_PARAMS)).toString();
                }
                SerialClob jsonClob = new SerialClob(jsonParamString.toCharArray());
                updateTemplateStmt.setClob(1, jsonClob);
                insertTemplateStmt.setClob(3, jsonClob);

                checkRequiredProp(check, PROP_CHECKS_INTERVAL);
                selTemplateStmt.setInt(3, (Integer) check.get(PROP_CHECKS_INTERVAL));
                insertTemplateStmt.setInt(4, (Integer) check.get(PROP_CHECKS_INTERVAL));

                checkRequiredProp(check, PROP_CHECKS_RETRY_INT);
                selTemplateStmt.setInt(4, (Integer) check.get(PROP_CHECKS_RETRY_INT));
                insertTemplateStmt.setInt(5, (Integer) check.get(PROP_CHECKS_RETRY_INT));

                checkRequiredProp(check, PROP_CHECKS_RETRY_ATT);
                selTemplateStmt.setInt(5, (Integer) check.get(PROP_CHECKS_RETRY_ATT));
                insertTemplateStmt.setInt(6, (Integer) check.get(PROP_CHECKS_RETRY_ATT));

                checkRequiredProp(check, PROP_CHECKS_TIMEOUT);
                selTemplateStmt.setInt(6, (Integer) check.get(PROP_CHECKS_TIMEOUT));
                insertTemplateStmt.setInt(7, (Integer) check.get(PROP_CHECKS_TIMEOUT));

                ResultSet selResult = selTemplateStmt.executeQuery();
                if(selResult.next()){
                    updateTemplateStmt.setInt(2, selResult.getInt(1));
                    updateTemplateStmt.execute();
                    templateIdMap.put(checkName, selResult.getInt(1));
                }else{
                    insertTemplateStmt.executeUpdate();
                    ResultSet genKeys = insertTemplateStmt.getGeneratedKeys();
                    if(!genKeys.next()){
                        throw new RuntimeException("No generated keys");
                    }
                    templateIdMap.put(checkName, genKeys.getInt(1));
                }
            }

            //disable all checks
            conn.createStatement().executeUpdate("UPDATE checks SET active=0");

            //remove all grids
            //NOTE: Remove this if ever have foreign keys to this value
            conn.createStatement().executeUpdate("DELETE FROM grids");
            conn.createStatement().executeUpdate("DELETE FROM checkStateDefs");
            
            //load reports
            Map<String, Rule> ruleIdMap = ConfigLoader.loadReport(config);
            Map<String, Rule> gridReportMap = new HashMap<String, Rule>();
            Rule defaultReport = null;
            if(config.containsKey(PROP_DEFAULT_REPORT) && config.get(PROP_DEFAULT_REPORT) != null){
                String defaultReportId = config.get(PROP_DEFAULT_REPORT) + "";
                if(!ruleIdMap.containsKey(defaultReportId) || ruleIdMap.get(defaultReportId) == null){
                    throw new RuntimeException("Invalid " + PROP_DEFAULT_REPORT + "specifed. Id '" + defaultReportId + "'does not exist" );
                }
                defaultReport = ruleIdMap.get(defaultReportId);
                gridReportMap.put(Madalert.RULE_DEFAULT_KEY, defaultReport);
            }
            
            //parse grids and update database
            PreparedStatement insertGridStmt = conn.prepareStatement("INSERT INTO grids VALUES(DEFAULT, ?, ?, ?, ?, ?, ?)");            
            PreparedStatement selCheckStmt = conn.prepareStatement("SELECT id FROM checks WHERE " +
                    "gridName=? AND rowName=? AND colName =? AND checkName=? AND checkTemplateId=?");
            PreparedStatement updateCheckStmt = conn.prepareStatement("UPDATE checks SET description=?, rowOrder=?, colOrder=?, active=1 WHERE id=?");
            PreparedStatement insertCheckStmt = conn.prepareStatement("INSERT INTO checks VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, " + CheckConstants.RESULT_NOTRUN  + "," + CheckConstants.RESULT_NOTRUN  + ", 'Check has not run', 0, 1)");
            PreparedStatement insertCheckStateDefsStmt = conn.prepareStatement("INSERT INTO checkStateDefs VALUES(DEFAULT, ?, ?, ?, ?)");
            for(Map gridMap : gridList){
                String rowOrder = (String) gridMap.get(PROP_GRIDS_ROW_ORDER);
                String colOrder = (String) gridMap.get(PROP_GRIDS_COL_ORDER);

                checkRequiredProp(gridMap, PROP_GRIDS_NAME);
                checkRequiredProp(gridMap, PROP_GRIDS_ROWS);
                checkRequiredProp(gridMap, PROP_GRIDS_EXCL_SELF);
                checkRequiredProp(gridMap, PROP_GRIDS_COL_ALG);
                checkRequiredProp(gridMap, PROP_GRIDS_COLS);
                checkRequiredProp(gridMap, PROP_GRIDS_CHECKS);
                checkRequiredProp(gridMap, PROP_GRIDS_STATUS_LABELS);
                // checkRequiredProp(gridMap, PROP_GRIDS_ROW_ORDER);
                // checkRequiredProp(gridMap, PROP_GRIDS_COL_ORDER);
                
                //load reports if we have any
                if(gridMap.containsKey(PROP_GRIDS_REPORT) && gridMap.get(PROP_GRIDS_REPORT) != null){
                    String reportId = gridMap.get(PROP_GRIDS_REPORT) + "";
                    if(!ruleIdMap.containsKey(reportId) || ruleIdMap.get(reportId) == null){
                        throw new RuntimeException("Unable to report with id " + reportId);
                    }
                    gridReportMap.put(gridMap.get(PROP_GRIDS_NAME)+"", ruleIdMap.get(reportId));
                }else if(defaultReport != null){
                    gridReportMap.put(gridMap.get(PROP_GRIDS_NAME)+"", defaultReport);
                }
                
                String colAlg = ((String)gridMap.get(PROP_GRIDS_COL_ALG)).toLowerCase();
                int exclSelf = (Integer)gridMap.get(PROP_GRIDS_EXCL_SELF);
                Map<String, List<String>> exclChecks = new HashMap<String, List<String>>();
                if(gridMap.containsKey(PROP_GRIDS_EXCL_CHECKS) && 
                        gridMap.get(PROP_GRIDS_EXCL_CHECKS) != null){
                    exclChecks = (Map<String, List<String>>) gridMap.get(PROP_GRIDS_EXCL_CHECKS);
                }
                
                //check groups
                checkRequiredProp(groupMap, (String) gridMap.get(PROP_GRIDS_ROWS));
                checkRequiredProp(groupMap, (String) gridMap.get(PROP_GRIDS_COLS));

                //Load rows and columns and get in right order
                List<String> rows = new ArrayList<String>();
                for(Object tmpRow : (List<Object>) groupMap.get(gridMap.get(PROP_GRIDS_ROWS))){
                    rows.add(tmpRow+"");//convert to string in case not quoted in file
                }
                
                if(ORDER_ALPHA.equals(rowOrder)){
                    rows = ConfigLoader.sortDimension(rows, dimensionLabelMap);
                }

                List<String> cols = new ArrayList<String>();
                List<String> tmpCols = new ArrayList<String>();
                for(Object tmpCol :  (List<Object>) groupMap.get(gridMap.get(PROP_GRIDS_COLS))){
                    cols.add(tmpCol+"");//convert to string in case not quoted in file
                    tmpCols.add(tmpCol+"");
                }
                if(ORDER_ALPHA.equals(colOrder)){
                    tmpCols = ConfigLoader.sortDimension(tmpCols, dimensionLabelMap);
                }
                HashMap<String,Integer> colOrderMap = new HashMap<String,Integer>();
                for(int i = 0; i < tmpCols.size(); i++){
                    colOrderMap.put(tmpCols.get(i), i);
                }

                //load up grids table
                Map<String,Object> statusLabelMap = (Map<String, Object>) gridMap.get(PROP_GRIDS_STATUS_LABELS);
                insertGridStmt.setString(1, (String)gridMap.get(PROP_GRIDS_NAME));
                insertGridStmt.setString(2, ConfigLoader.genStatusLabel(statusLabelMap, PROP_GRIDS_STATUS_LABELS_OK));
                insertGridStmt.setString(3, ConfigLoader.genStatusLabel(statusLabelMap, PROP_GRIDS_STATUS_LABELS_WARNING));
                insertGridStmt.setString(4, ConfigLoader.genStatusLabel(statusLabelMap, PROP_GRIDS_STATUS_LABELS_CRITICAL));
                insertGridStmt.setString(5, ConfigLoader.genStatusLabel(statusLabelMap, PROP_GRIDS_STATUS_LABELS_UNKNOWN));
                insertGridStmt.setString(6, ConfigLoader.genStatusLabel(statusLabelMap, PROP_GRIDS_STATUS_LABELS_NOTRUN));
                insertGridStmt.executeUpdate();
                
                //load up custom states
                if(statusLabelMap.containsKey(PROP_GRIDS_STATUS_LABELS_EXTRA) && statusLabelMap.get(PROP_GRIDS_STATUS_LABELS_EXTRA) != null){
                    List<Map<String,Object>> extraStatusDefs = (List<Map<String,Object>>) statusLabelMap.get(PROP_GRIDS_STATUS_LABELS_EXTRA);
                    for(Map<String, Object> extraStatusDef: extraStatusDefs){
                        insertCheckStateDefsStmt.setString(1, (String) gridMap.get(PROP_GRIDS_NAME));
                        //support quoted and unquoted integer
                        insertCheckStateDefsStmt.setInt(2, Integer.parseInt(extraStatusDef.get(PROP_GRIDS_STATUS_LABELS_EXTRA_VALUE)+""));
                        insertCheckStateDefsStmt.setString(3, (String)extraStatusDef.get(PROP_GRIDS_STATUS_LABELS_EXTRA_SHORT_NAME));
                        insertCheckStateDefsStmt.setString(4, (String)extraStatusDef.get(PROP_GRIDS_STATUS_LABELS_EXTRA_DESCR));
                        insertCheckStateDefsStmt.executeUpdate();
                    }
                }
                
                
                //load up check database table
                selCheckStmt.setString(1, (String)gridMap.get(PROP_GRIDS_NAME));
                for(int ri = 0; ri < rows.size(); ri++){
                    String row = rows.get(ri);
                    boolean rowColFound = false;
                    selCheckStmt.setString(2, row);
                    for(String col : cols){
                        //compare column and row
                        if(col.equals(row) && exclSelf == 1){
                            rowColFound = true;
                            continue;
                        }else if(col.equals(row)){
                            rowColFound = true;
                        }

                        //check if we should skip
                        if(exclChecks.containsKey(row) && exclChecks.get(row) != null){
                            if(exclChecks.get(row).contains(col) || 
                                    exclChecks.get(row).contains(EXCL_CHECKS_ALL)){
                                continue; 
                            }

                        }else if(exclChecks.containsKey(EXCL_CHECKS_DEFAULT) && 
                                exclChecks.get(EXCL_CHECKS_DEFAULT) != null){
                            if(exclChecks.get(EXCL_CHECKS_DEFAULT).contains(col) || 
                                    exclChecks.get(EXCL_CHECKS_DEFAULT).contains(EXCL_CHECKS_ALL)){
                                continue; 
                            }
                        }

                        //determine if we need to add a check
                        if(colAlg.equals(ALG_AFTER) && !rowColFound){
                            continue;
                        }else if(colAlg.equals(ALG_BEFORE) && rowColFound){
                            break;
                        }

                        //Handle database insert/update
                        selCheckStmt.setString(3, col);
                        for(String checkName : (List<String>)gridMap.get(PROP_GRIDS_CHECKS)){
                            if(!checkMap.containsKey(checkName) || checkMap.get(checkName) == null){
                                throw new RuntimeException("Invalid check name " + checkName + " provided in grid definition.");
                            }
                            String checkNiceName = (String)checkMap.get(checkName).get(PROP_CHECKS_NAME);
                            String checkDescrName = (String)checkMap.get(checkName).get(PROP_CHECKS_DESCRIPTION);
                            checkRequiredProp(templateIdMap, checkName);
                            selCheckStmt.setString(4, checkNiceName);
                            selCheckStmt.setInt(5, templateIdMap.get(checkName));
                            ResultSet selResult = selCheckStmt.executeQuery();
                            if(selResult.next()){
                                log.debug("Updated check " + checkName);
                                updateCheckStmt.setString(1, formatCheckDescription(checkDescrName, row, col, dimensionMap));
                                updateCheckStmt.setInt(2, ri);
                                updateCheckStmt.setInt(3, colOrderMap.get(col));
                                updateCheckStmt.setInt(4, selResult.getInt(1));
                                updateCheckStmt.executeUpdate();
                            }else{
                                log.debug("Added check " + checkName);
                                insertCheckStmt.setInt(1, templateIdMap.get(checkName));
                                insertCheckStmt.setString(2, (String)gridMap.get(PROP_GRIDS_NAME));
                                insertCheckStmt.setString(3, row);
                                insertCheckStmt.setString(4, col);
                                insertCheckStmt.setString(5, checkNiceName);
                                insertCheckStmt.setInt(6, ri);
                                insertCheckStmt.setInt(7, colOrderMap.get(col));
                                insertCheckStmt.setString(8, formatCheckDescription(checkDescrName, row, col, dimensionMap));
                                insertCheckStmt.executeUpdate();
                            }

                        }
                    }
                }
            }
            conn.close();
            
            //set report rules
            if(!gridReportMap.isEmpty()){
                Madalert.setRules(gridReportMap);
            }
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            e.printStackTrace();
            throw new RuntimeException("Error loading database: " + e.getMessage());
        }

        return checkTypeClassMap;
    }
    
    private static String notifyKey(String notifyName, String notifyType) {
        return notifyName + "::::" + notifyType;
    }

    static public  Map<String, Rule> loadReport(Map config){
        Map<String, Rule> ruleIdMap = new HashMap<String, Rule>();
        //make sure we have any reports defined
        if(!config.containsKey(PROP_REPORTS) || config.get(PROP_REPORTS) == null){
            return ruleIdMap;
        }
        
        //loop through reports
        List<Map> reportList = (List<Map>) config.get(PROP_REPORTS);
        for(Map report : reportList){
            checkRequiredProp(report, PROP_REPORTS_ID);
            checkRequiredProp(report, PROP_REPORTS_RULE);
            String reportId = report.get(PROP_REPORTS_ID) + "";
            if(ruleIdMap.containsKey(reportId) && ruleIdMap.get(reportId) != null){
                throw new RuntimeException("Found two reports with same ID '" + 
                        reportId + "'. IDs must be unique.");
            }
            ruleIdMap.put(reportId, parseRule((Map)report.get(PROP_REPORTS_RULE)));
        }
        
        return ruleIdMap;
    }
    
    static private Rule parseRule(Map rule){
        checkRequiredProp(rule, PROP_REPORTS_RULE_TYPE);
        String type = rule.get(PROP_REPORTS_RULE_TYPE) + "";
        if(type.equals(PROP_REPORTS_RULE_TYPE_MATCH_FIRST)){
            checkRequiredProp(rule, PROP_REPORTS_RULES);
            return Madalert.matchFirst(parseRules((List<Map>)rule.get(PROP_REPORTS_RULES)));
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_MATCH_ALL)){
            checkRequiredProp(rule, PROP_REPORTS_RULES);
            return Madalert.matchAll(parseRules((List<Map>)rule.get(PROP_REPORTS_RULES)));
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_FOREACH_SITE)){
            checkRequiredProp(rule, PROP_REPORTS_RULE);
            return Madalert.forEachSite(parseSiteRule((Map)rule.get(PROP_REPORTS_RULE)));
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_SITERULE)){
            checkRequiredProp(rule, PROP_REPORTS_RULE_SITE);
            checkRequiredProp(rule, PROP_REPORTS_RULE_SELECTOR);
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH);
            checkRequiredProp(rule, PROP_REPORTS_RULE_PROBLEM);
            return Madalert.rule(parseSiteTestSet((Map)rule.get(PROP_REPORTS_RULE_SELECTOR)), 
                    parseStatusMatcher((Map)rule.get(PROP_REPORTS_RULE_MATCH)), 
                    parseProblem((Map)rule.get(PROP_REPORTS_RULE_PROBLEM))).site(rule.get(PROP_REPORTS_RULE_SITE) + "");
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_RULE)){
            checkRequiredProp(rule, PROP_REPORTS_RULE_SELECTOR);
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH);
            checkRequiredProp(rule, PROP_REPORTS_RULE_PROBLEM);
            return Madalert.rule(parseTestSet((Map)rule.get(PROP_REPORTS_RULE_SELECTOR)), 
                    parseStatusMatcher((Map)rule.get(PROP_REPORTS_RULE_MATCH)), 
                    parseProblem((Map)rule.get(PROP_REPORTS_RULE_PROBLEM)));
        }
        
        throw new RuntimeException("Invalid rule type '" + type + "'");
    }

    private static SiteRule parseSiteRule(Map rule) {
        checkRequiredProp(rule, PROP_REPORTS_RULE_TYPE);
        String type = rule.get(PROP_REPORTS_RULE_TYPE) + "";
        if(type.equals(PROP_REPORTS_RULE_TYPE_MATCH_FIRST)){
            checkRequiredProp(rule, PROP_REPORTS_RULES);
            return Madalert.matchFirst(parseSiteRules((List<Map>)rule.get(PROP_REPORTS_RULES)));
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_MATCH_ALL)){
            checkRequiredProp(rule, PROP_REPORTS_RULES);
            return Madalert.matchAll(parseSiteRules((List<Map>)rule.get(PROP_REPORTS_RULES)));
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_FOREACH_SITE)){
            throw new RuntimeException("Cannot nest " + PROP_REPORTS_RULE_TYPE_FOREACH_SITE + " rules");
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_SITERULE)){
            throw new RuntimeException("Cannot nest " + PROP_REPORTS_RULE_TYPE_SITERULE + " rules in " + PROP_REPORTS_RULE_TYPE_FOREACH_SITE);
        }else if(type.equals(PROP_REPORTS_RULE_TYPE_RULE)){
            checkRequiredProp(rule, PROP_REPORTS_RULE_SELECTOR);
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH);
            checkRequiredProp(rule, PROP_REPORTS_RULE_PROBLEM);
            return Madalert.rule(parseSiteTestSet((Map)rule.get(PROP_REPORTS_RULE_SELECTOR)), 
                    parseStatusMatcher((Map)rule.get(PROP_REPORTS_RULE_MATCH)), 
                    parseProblem((Map)rule.get(PROP_REPORTS_RULE_PROBLEM)));
        }
        
        throw new RuntimeException("Invalid rule type '" + type + "'");
    }
    
    private static TestSet parseTestSet(Map rule) {
        checkRequiredProp(rule, PROP_REPORTS_RULE_SELECTOR_TYPE);
        String type = rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE) + "";
        if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_GRID)){
            return Madalert.forAllSites();
        }else if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_SITE) ||
                type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_ROW) ||
                type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_COLUMN) ||
                type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL) ||
                type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK)){
            throw new RuntimeException("You may only use selector type '" + 
                    type + "' within a " + PROP_REPORTS_RULE_TYPE_FOREACH_SITE + "rule");
        }
        
        throw new RuntimeException("Invalid rule selector type '" + type + "'");
    }
    
    private static SiteTestSet parseSiteTestSet(Map rule) {
        checkRequiredProp(rule, PROP_REPORTS_RULE_SELECTOR_TYPE);
        String type = rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE) + "";
        if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_GRID)){
            throw new RuntimeException("You may only use selector type '" + 
                    type + "' outside a " + PROP_REPORTS_RULE_TYPE_FOREACH_SITE + "rule");
        }else if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_SITE)){
            return Madalert.forSite();
        }else if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_ROW)){
            return Madalert.forRow();
        }else if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_COLUMN)){
            return Madalert.forColumn();
        }else if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL)){
            String rowSite = null;
            String colSite = null;
            int rowCheck = -1;
            int colCheck = -1;
            if(rule.containsKey(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_ROWSITE) && rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_ROWSITE) != null){
                rowSite = rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_ROWSITE) + "";
            }
            if(rule.containsKey(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_COLSITE) && rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_COLSITE) != null){
                colSite = rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_COLSITE) + "";
            }
            try{
                if(rule.containsKey(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX) && rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX) != null){
                    rowCheck = Integer.parseInt(rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX) + "");
                }
                if(rule.containsKey(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX) && rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX) != null){
                    colCheck = Integer.parseInt(rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX) + "");
                }
            }catch(Exception e){
                throw new RuntimeException("Both " + PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX + 
                        " and " + PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX + " must be an integer "
                        + "when used in a selector of type " + type);
            }
            if(rowSite == null && colSite == null){
                throw new RuntimeException("Must specify either " + PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_ROWSITE + 
                        " or " + PROP_REPORTS_RULE_SELECTOR_TYPE_CELL_COLSITE + "when using a selector of type " + type);
            }
            return Madalert.forCell(rowSite, colSite, rowCheck, colCheck);
        }else if(type.equals(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK)){
            int rowCheck = -1;
            int colCheck = -1;
            try{
                if(rule.containsKey(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX) && rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX) != null){
                    rowCheck = Integer.parseInt(rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX) + "");
                }
                if(rule.containsKey(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX) && rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX) != null){
                    colCheck = Integer.parseInt(rule.get(PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX) + "");
                }
            }catch(Exception e){
                throw new RuntimeException("Both " + PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX + 
                        " and " + PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX + " must be an integer "
                        + "when used in a selector of type " + type);
            }
            if(rowCheck < 0 && colCheck < 0){
                throw new RuntimeException("Must specify either " + PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_ROWINDEX + 
                        " or " + PROP_REPORTS_RULE_SELECTOR_TYPE_CHECK_COLINDEX + "when using a selector of type " + type);
            }
            return Madalert.forCheck(rowCheck, colCheck);
        }
        
        throw new RuntimeException("Invalid rule selector type '" + type + "'");
    }
    
    private static StatusMatcher parseStatusMatcher(Map rule) {
        checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH_TYPE);
        String type = rule.get(PROP_REPORTS_RULE_MATCH_TYPE) + "";
        if(type.equals(PROP_REPORTS_RULE_MATCH_TYPE_STATUS)){
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH_TYPE_STATUS_STATUS);
            try{
                return Madalert.matchStatus(Integer.parseInt(rule.get(PROP_REPORTS_RULE_MATCH_TYPE_STATUS_STATUS)+""));
            }catch(Exception e){
                throw new RuntimeException("The match 'status' property must be an integer");
            }
        }else if(type.equals(PROP_REPORTS_RULE_MATCH_TYPE_STATUSTHRESH)){
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH_TYPE_STATUS_STATUS);
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH_TYPE_STATUSTHRESH_THRESH);
            try{
                return Madalert.matchStatus(
                        Integer.parseInt(rule.get(PROP_REPORTS_RULE_MATCH_TYPE_STATUS_STATUS)+""),
                        Double.parseDouble(rule.get(PROP_REPORTS_RULE_MATCH_TYPE_STATUSTHRESH_THRESH)+""));
            }catch(Exception e){
                throw new RuntimeException("The match 'status' and 'threshold' property must be an integer and floating point number repspectively");
            }
        }else if(type.equals(PROP_REPORTS_RULE_MATCH_TYPE_STATUSWEIGHTTHRESH)){
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH_TYPE_STATUSWEIGHTTHRESH_STATUSES);
            checkRequiredProp(rule, PROP_REPORTS_RULE_MATCH_TYPE_STATUSTHRESH_THRESH);
            try{
                List statusesConfig = (List)rule.get(PROP_REPORTS_RULE_MATCH_TYPE_STATUSWEIGHTTHRESH_STATUSES);
                double[] statuses = new double[statusesConfig.size()];
                for(int i =0; i < statusesConfig.size(); i++){
                    statuses[i] = Double.parseDouble(statusesConfig.get(i)+"");
                }
                return Madalert.matchStatus(
                        statuses,
                        Double.parseDouble(rule.get(PROP_REPORTS_RULE_MATCH_TYPE_STATUSTHRESH_THRESH)+""));
            }catch(Exception e){
                throw new RuntimeException("The match 'statuses' must be a list of floating points and 'threshold' property must be a floating point number ");
            }
        }
        
        throw new RuntimeException("Invalid rule match type '" + type + "'");
    }
    
    private static Problem parseProblem(Map rule) {
        checkRequiredProp(rule, PROP_REPORTS_RULE_PROBLEM_SEVERITY);
        checkRequiredProp(rule, PROP_REPORTS_RULE_PROBLEM_CATEGORY);
        checkRequiredProp(rule, PROP_REPORTS_RULE_PROBLEM_MSG);
        int severity;
        try{
            severity = Integer.parseInt(rule.get(PROP_REPORTS_RULE_PROBLEM_SEVERITY)+"");
        }catch(Exception e){
            throw new RuntimeException("The 'severity' property must be an integer");
        }
        List<String> solutions = new ArrayList<String>();
        if(rule.containsKey(PROP_REPORTS_RULE_PROBLEM_SOLUTIONS) && rule.get(PROP_REPORTS_RULE_PROBLEM_SOLUTIONS) != null){
            solutions = (List<String>) rule.get(PROP_REPORTS_RULE_PROBLEM_SOLUTIONS);
        }
        return new Problem(rule.get(PROP_REPORTS_RULE_PROBLEM_MSG)+"",
                        severity,
                        rule.get(PROP_REPORTS_RULE_PROBLEM_CATEGORY)+"",
                        solutions);
    }
    
    private static SiteRule[] parseSiteRules(List<Map> configRules) {
        SiteRule[] rules = new SiteRule[configRules.size()];
        for(int i = 0; i < configRules.size(); i++){
            rules[i] = parseSiteRule(configRules.get(i));
        }
        return rules;
    }
    
    private static List<Rule> parseRules(List<Map> configRules) {
        List<Rule> rules = new ArrayList<Rule>();
        for(Map configRule : configRules){
            rules.add(parseRule(configRule));
        }
        return rules;
    }

    /**
     * Sorts rows and/or columns alphabetically by label if provided or by ID otherwise
     * @param dimension the row or column list to sort
     * @param dimensionLabelMap the map of row/column ids to labels
     * @return the sorted list
     */
    private static List<String> sortDimension(List<String> dimension,
            HashMap<String, String> dimensionLabelMap) {
        final HashMap<String,String> tmpMap = new HashMap<String,String>();
        for (String d : dimension){
            if(dimensionLabelMap.containsKey(d) && dimensionLabelMap.get(d) != null){
                tmpMap.put(d, dimensionLabelMap.get(d));
            }else{
                tmpMap.put(d, d);
            }
        }
        List<String> tmpMapKeys = new ArrayList<String>();
        tmpMapKeys.addAll(tmpMap.keySet());
        Collections.sort(tmpMapKeys, new Comparator<String>(){
            public int compare(String s1, String s2) {
                return tmpMap.get(s1).compareTo(tmpMap.get(s2));
            }
        });
        
        return tmpMapKeys;
    }

    private static String genStatusLabel(Map<String, Object> statusLabelMap, String label) {
        if(!statusLabelMap.containsKey(label) || statusLabelMap.get(label) == null){
            return "";
        }
        return statusLabelMap.get(label)+"";
    }
    
    private static String replaceDimensionProps(String str, String type, String key1, String key2, HashMap<String, JSONObject> dimensionMap){
        if(!dimensionMap.containsKey(key1)){
            return str;
        }
        for(Object dimMapParam : dimensionMap.get(key1).keySet()){
            if(dimMapParam.equals(PROP_DIMENSION_MAP)){
                JSONObject rowColMap = dimensionMap.get(key1).getJSONObject(dimMapParam + "");
                JSONObject colMap = DimensionUtil.getJsonProp(rowColMap, key2);
                for(Object colProp : colMap.keySet()){
                    str = str.replaceAll("%" + type + ".map."+colProp, colMap.getString(colProp+""));
                }
            }else{
                str = str.replaceAll("%" + type + dimMapParam, dimensionMap.get(key1).getString(dimMapParam+""));
            }
        }
        return str;
    }
    private static String formatCheckDescription(String description, String rowName, String colName, HashMap<String, JSONObject> dimensionMap) {
        description = ConfigLoader.replaceDimensionProps(description, "row", rowName, colName, dimensionMap);
        description = ConfigLoader.replaceDimensionProps(description, "col", colName, rowName, dimensionMap);
        description = description.replaceAll("%row", rowName);
        description = description.replaceAll("%col", colName);
        return description;
    }

    private static Class loadClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = ConfigLoader.class.getClassLoader();
        Class checkClass = classLoader.loadClass(className);
        for(Class iface : checkClass.getInterfaces()){
            if(iface.getName().equals(Check.class.getName())){
                return checkClass;
            }
        }
        throw new RuntimeException("Class " + className + " does not implement Check interface");
    }

    static private void checkRequiredProp(Map config, String propName){
        if(!config.containsKey(propName) || config.get(propName) == null){
            throw new RuntimeException("The property '" + propName + "' is not defined");
        }
    }
    
    static public void loadNotifications(Map config,ComboPooledDataSource dataSource, Scheduler scheduler) throws SchedulerException{
        if(!config.containsKey(PROP_NOTIFICATIONS) || config.get(PROP_NOTIFICATIONS) == null){
            return;
        }
        
        Connection conn = null;
        try{
            conn = dataSource.getConnection();
            //load notifications if we have any
            HashMap<String, Integer> notifyNameMap = new HashMap<String, Integer>();
            PreparedStatement selNotifications = conn.prepareStatement("SELECT id, name, type FROM notifications");
            PreparedStatement insertNotifications = conn.prepareStatement("INSERT INTO " +
                    "notifications VALUES(DEFAULT, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement updateNotifications = conn.prepareStatement("UPDATE notifications SET params=? WHERE id=?");
            PreparedStatement delNotifications = conn.prepareStatement("DELETE FROM notifications WHERE id=?");
            PreparedStatement delNotificationProblems = conn.prepareStatement("DELETE FROM notificationProblems WHERE notificationId=?");
            //build map of current notification types
            ResultSet notifySelResult = selNotifications.executeQuery();
            while(notifySelResult.next()){
                String key = ConfigLoader.notifyKey(notifySelResult.getString(2), notifySelResult.getString(3));
                notifyNameMap.put(key, notifySelResult.getInt(1));
            }

            //insert new/update existing
            List<Map> notifyConfigList = (List<Map>) config.get(PROP_NOTIFICATIONS);
            int i = 0;
            for(Map notifyConfig : notifyConfigList){
                checkRequiredProp(notifyConfig, PROP_NOTIFICATIONS_NAME);
                checkRequiredProp(notifyConfig, PROP_NOTIFICATIONS_TYPE);
                checkRequiredProp(notifyConfig, PROP_NOTIFICATIONS_PARAMS);
                checkRequiredProp(notifyConfig, PROP_NOTIFICATIONS_SCHEDULE);
               
                //insert into database
                String notifyName = notifyConfig.get(PROP_NOTIFICATIONS_NAME) + "";
                String notifyType = notifyConfig.get(PROP_NOTIFICATIONS_TYPE) + "";
                if(!NotificationFactory.isValidType(notifyType)){
                    throw new RuntimeException("Invalid notification type " + notifyType);
                }
                String notifyParams = JSONObject.fromObject(notifyConfig.get(PROP_NOTIFICATIONS_PARAMS)).toString();
                SerialClob paramClob = new SerialClob(notifyParams.toCharArray());
                String notifyKey = ConfigLoader.notifyKey(notifyName, notifyType);
                int notificationId = -1;
                if(notifyNameMap.containsKey(notifyKey)){
                    //update
                    notificationId = notifyNameMap.get(notifyKey);
                    updateNotifications.setClob(1, paramClob);
                    updateNotifications.setInt(2, notificationId);
                    updateNotifications.executeUpdate();
                    notifyNameMap.remove(notifyKey);
                }else{
                    //insert
                    insertNotifications.setString(1, notifyName);
                    insertNotifications.setString(2, notifyType);
                    insertNotifications.setClob(3, paramClob);
                    insertNotifications.executeUpdate();
                    ResultSet genKeys = insertNotifications.getGeneratedKeys();
                    if(genKeys.next()){
                        notificationId = genKeys.getInt(1);
                    }else{
                        throw new RuntimeException("Notification insert failed to yield an auto-generated key");
                    }
                }
                
                //schedule job
                String schedule = notifyConfig.get(PROP_NOTIFICATIONS_SCHEDULE).toString().trim();
                schedule = "0 " + schedule; //match quartz special format by adding seconds
                int minSeverity = -1;
                if(notifyConfig.containsKey(PROP_NOTIFICATIONS_MINSEV) && 
                        notifyConfig.get(PROP_NOTIFICATIONS_MINSEV) != null){
                    try{
                        minSeverity = Integer.parseInt(notifyConfig.get(PROP_NOTIFICATIONS_MINSEV) + "");
                    }catch(Exception e){
                        throw new RuntimeException(PROP_NOTIFICATIONS_MINSEV + " must be an integer");
                    }
                }
                int frequency = -1;
                if(notifyConfig.containsKey(PROP_NOTIFICATIONS_PROBFREQ) && 
                        notifyConfig.get(PROP_NOTIFICATIONS_PROBFREQ) != null){
                    try{
                        frequency = Integer.parseInt(notifyConfig.get(PROP_NOTIFICATIONS_PROBFREQ) + "");
                    }catch(Exception e){
                        throw new RuntimeException(PROP_NOTIFICATIONS_PROBFREQ + " must be an integer");
                    }
                }
                int resolveAfter = -1;
                if(notifyConfig.containsKey(PROP_NOTIFICATIONS_PROBRESOLV) &&
                        notifyConfig.get(PROP_NOTIFICATIONS_PROBRESOLV) != null){
                    try{
                        resolveAfter = Integer.parseInt(notifyConfig.get(PROP_NOTIFICATIONS_PROBRESOLV) + "");
                    }catch(Exception e){
                        throw new RuntimeException(PROP_NOTIFICATIONS_PROBRESOLV + " must be an integer");
                    }
                }
                HashMap<String,Boolean> dashboardFilters = new HashMap<String,Boolean>();
                HashMap<String,Boolean> gridFilters = new HashMap<String,Boolean>(); 
                HashMap<String,Boolean> siteFilters = new HashMap<String,Boolean>(); 
                HashMap<String,Boolean> categoryFilters = new HashMap<String,Boolean>(); 
                if(notifyConfig.containsKey(PROP_NOTIFICATIONS_FILTERS) && 
                        notifyConfig.get(PROP_NOTIFICATIONS_FILTERS) != null){
                    for(Map filter: (List<Map>)notifyConfig.get(PROP_NOTIFICATIONS_FILTERS)){
                        checkRequiredProp(filter, PROP_NOTIFICATIONS_FILTERS_TYPE);
                        checkRequiredProp(filter, PROP_NOTIFICATIONS_FILTERS_VALUE);
                        if(PROP_NOTIFICATIONS_FILTERS_TYPE_DASHBOARD.equals(filter.get(PROP_NOTIFICATIONS_FILTERS_TYPE)+"")){
                            dashboardFilters.put(filter.get(PROP_NOTIFICATIONS_FILTERS_VALUE) + "", true);
                        }else if(PROP_NOTIFICATIONS_FILTERS_TYPE_GRID.equals(filter.get(PROP_NOTIFICATIONS_FILTERS_TYPE)+"")){
                            gridFilters.put(filter.get(PROP_NOTIFICATIONS_FILTERS_VALUE)+"", true);
                        }else if(PROP_NOTIFICATIONS_FILTERS_TYPE_SITE.equals(filter.get(PROP_NOTIFICATIONS_FILTERS_TYPE)+"")){
                            siteFilters.put(filter.get(PROP_NOTIFICATIONS_FILTERS_VALUE)+"", true);
                        }else if(PROP_NOTIFICATIONS_FILTERS_TYPE_CATEGORY.equals(filter.get(PROP_NOTIFICATIONS_FILTERS_TYPE)+"")){
                            categoryFilters.put(filter.get(PROP_NOTIFICATIONS_FILTERS_VALUE)+"", true);
                        }else{
                            throw new RuntimeException("Invalid " + PROP_NOTIFICATIONS_FILTERS_TYPE + ": " + PROP_NOTIFICATIONS_FILTERS_VALUE);
                        }
                    }
                }
                Notification notifier = NotificationFactory.create(notifyName, notifyType, Json.createReader(new StringReader(notifyParams)).readObject());
                JobDataMap dataMap = new JobDataMap();
                dataMap.put("notificationId", notificationId);
                dataMap.put("notifier", notifier);
                dataMap.put("minSeverity", minSeverity);
                dataMap.put("frequency", frequency);
                dataMap.put("resolveAfter", resolveAfter);
                dataMap.put("dashboardFilters", dashboardFilters);
                dataMap.put("gridFilters", gridFilters);
                dataMap.put("siteFilters", siteFilters);
                dataMap.put("categoryFilters", categoryFilters);
                CronTrigger cronTrigger = newTrigger()
                        .withIdentity("NotifyTrigger"+i, "NOTIFY")
                        .withSchedule(cronSchedule(schedule))
                        .build();
                JobDetail jobDetail = newJob(NotifyJob.class)
                        .withIdentity("NotifyJob"+i, "NOTIFY")
                        .usingJobData(dataMap)
                        .build();
                scheduler.scheduleJob(jobDetail, cronTrigger);
                i++;
            }

            //delete old
            for(String notifyName : notifyNameMap.keySet()){
                delNotificationProblems.setInt(1, notifyNameMap.get(notifyName));
                delNotificationProblems.executeUpdate();
                delNotifications.setInt(1, notifyNameMap.get(notifyName));
                delNotifications.executeUpdate();
            }
            
            //close db
            conn.close();
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            e.printStackTrace();
            throw new RuntimeException("Error loading database: " + e.getMessage());
        }
    }
}

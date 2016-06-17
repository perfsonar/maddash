package net.es.maddash;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.UriInfo;


import org.apache.log4j.Logger;

import net.es.maddash.checks.CheckConstants;
import net.es.maddash.madalert.Madalert;
import net.es.maddash.madalert.Report;
import net.es.maddash.utils.RESTUtil;
import net.es.maddash.utils.URIUtil;
import net.es.maddash.www.rest.AdminEventsResource;
import net.es.maddash.www.rest.AdminScheduleResource;
import net.es.maddash.www.rest.CheckResource;

/**
 * Class that handles database access to resources and returns them as JSON objects
 * 
 * @author Andy Lake<alake@es.net>
 *
 */
public class ResourceManager {
    private Logger log = Logger.getLogger(ResourceManager.class);
    private Logger netlogger = Logger.getLogger("netlogger");
    
    /**
     * Returns a list of dashboards 
     * 
     * @return List of dashboards as JSON
     */
    public JsonObject getDashboards() {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.ResourceManager.getDashboards"));
        JsonObject json = Json.createObjectBuilder()
                                .add("dashboards", MaDDashGlobals.getInstance().getDashboards())
                                .build();
        netlogger.info(netLog.end("maddash.ResourceManager.getDashboards"));
        return json;
    }
    
    /**
     * Returns a list of grids 
     * 
     * @return List of grids as JSON
     */
    public JsonObject getGrids(UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getGrids"));
            JsonObjectBuilder json = Json.createObjectBuilder();
            JsonArrayBuilder jsonGridList = Json.createArrayBuilder();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet grids = stmt.executeQuery("SELECT DISTINCT gridName FROM grids");
            while(grids.next()){
                jsonGridList.add(Json.createObjectBuilder()
                        .add("name", grids.getString(1))
                        .add("uri", "/" + uriInfo.getPath() + "/" + URIUtil.normalizeURIPart(grids.getString(1))));
            }
            conn.close(); 
            json.add("grids", jsonGridList);
            netlogger.info(netLog.end("maddash.ResourceManager.getGrids"));
            return json.build();
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.ResourceManager.getGrids", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Returns a list of rows 
     * 
     * @return List of rows as JSON
     */
    public JsonObject getRows() {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getRows"));
            JsonObjectBuilder json = Json.createObjectBuilder();
            JsonArrayBuilder jsonList = Json.createArrayBuilder();
            HashMap<String,String> nameMap = new HashMap<String,String>();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet sqlResults = stmt.executeQuery("SELECT DISTINCT c.rowName, d.value FROM checks " +
                    "AS c INNER JOIN dimensions AS d ON d.configIdent=c.rowName WHERE d.keyName='label'");
            while(sqlResults.next()){
                nameMap.put(sqlResults.getString(1), sqlResults.getString(2));
            }
            ResultSet sqlResults2 = conn.createStatement().executeQuery("SELECT DISTINCT rowName FROM checks WHERE active=1");
            while(sqlResults2.next()){
                JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                        .add("id", sqlResults2.getString(1));
                if(nameMap.containsKey(sqlResults2.getString(1))){
                    jsonObj.add("name", nameMap.get(sqlResults2.getString(1)));
                }else{
                    jsonObj.add("name", sqlResults2.getString(1));
                }
                jsonList.add(jsonObj);
            }
            conn.close(); 
            json.add("rows", jsonList);
            netlogger.info(netLog.end("maddash.ResourceManager.getRows"));
            return json.build();
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.ResourceManager.getRows", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Returns a list of columns 
     * 
     * @return List of columns as JSON
     */
    public JsonObject getColumns() {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getColumns"));
            JsonObjectBuilder json = Json.createObjectBuilder();
            JsonArrayBuilder jsonList = Json.createArrayBuilder();
            HashMap<String,String> nameMap = new HashMap<String,String>();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet sqlResults = stmt.executeQuery("SELECT DISTINCT c.colName, d.value FROM checks " +
                    "AS c INNER JOIN dimensions AS d ON d.configIdent=c.colName WHERE d.keyName='label'");
            while(sqlResults.next()){
                nameMap.put(sqlResults.getString(1), sqlResults.getString(2));
            }
            ResultSet sqlResults2 = conn.createStatement().executeQuery("SELECT DISTINCT colName FROM checks WHERE active=1");
            while(sqlResults2.next()){
                JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                        .add("id", sqlResults2.getString(1));
                if(nameMap.containsKey(sqlResults2.getString(1))){
                    jsonObj.add("name", nameMap.get(sqlResults2.getString(1)));
                }else{
                    jsonObj.add("name", sqlResults2.getString(1));
                }
                jsonList.add(jsonObj);
            }
            conn.close(); 
            json.add("columns", jsonList);
            netlogger.info(netLog.end("maddash.ResourceManager.getColumns"));
            return json.build();
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.ResourceManager.getColumns", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Returns a list of checks 
     * 
     * @return List of checks as JSON
     */
    public JsonObject getChecks() {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getChecks"));
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            JsonObjectBuilder json = Json.createObjectBuilder();
            JsonArrayBuilder jsonList = Json.createArrayBuilder();
            ResultSet sqlResults = conn.createStatement().executeQuery("SELECT DISTINCT checkName FROM checks WHERE active=1");
            while(sqlResults.next()){
                jsonList.add(Json.createObjectBuilder().add("name", sqlResults.getString(1)).build());
            }
            conn.close(); 
            json.add("checks", jsonList);
            netlogger.info(netLog.end("maddash.ResourceManager.getChecks"));
            return json.build();
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.ResourceManager.getChecks", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Returns the full grid specified.
     * 
     * @param gridId the ID of the grid to query
     */
    public JsonObject getGrid(String gridId, UriInfo uriInfo) {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.ResourceManager.getGrid"));
        JsonObjectBuilder json = null;
        try {
            String gridName = URIUtil.decodeUriPart(gridId);
            DBMesh mesh = new DBMesh(gridName, uriInfo.getPath());
            Report report = Madalert.lookupRule(gridName).createReport(mesh);
            json = mesh.toJsonBuilder().add("report", report.toJson());
        } catch (Exception e) {
            netlogger.error(netLog.error("maddash.ResourceManager.getGrid", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        netlogger.info(netLog.end("maddash.ResourceManager.getGrid"));
        return json.build();
    }

    /**
     * Returns a grid row
     * 
     * @param gridId the id of the grid
     * @param rowId the id of the row
     * @param uriInfo the URI used to request this object
     * @return a JSON representation of the row
     */
    public JsonObject getRow(String gridId, String rowId, UriInfo uriInfo){
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder json = null;
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getRow"));
            String gridName = URIUtil.decodeUriPart(gridId);
            String rowName = URIUtil.decodeUriPart(rowId);
            json = Json.createObjectBuilder();
            JsonArrayBuilder jsonColList = Json.createArrayBuilder();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT colName FROM checks " +
                    "WHERE gridName=? AND rowName=? AND active=1");
            stmt.setString(1, gridName);
            stmt.setString(2, rowName);
            boolean isEmpty = true;
            ResultSet cols = stmt.executeQuery();
            while(cols.next()){
                isEmpty = false;
                jsonColList.add(Json.createObjectBuilder()
                                    .add("name", cols.getString(1))
                                    .add("uri", "/" + uriInfo.getPath() + "/" +
                                            URIUtil.normalizeURIPart(cols.getString(1))));
            }
            //no row found
            if(isEmpty){
                netlogger.info(netLog.end("maddash.ResourceManager.getRow"));
                return null;
            }
            conn.close();
            json.add("cells", jsonColList);
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.ResourceManager.getRow", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        
        netlogger.info(netLog.end("maddash.ResourceManager.getRow"));
        return json.build();
    }
    
    /**
     * Returns a grid cell
     * 
     * @param gridId the id of the grid
     * @param rowId the id of the row
     * @param colId the id of the column
     * @param uriInfo the URI used to request this object
     * @return a JSON representation of the row
     */
    public JsonObject getCell(String gridId, String rowId, String colId, UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getCell"));
            String gridName = URIUtil.decodeUriPart(gridId);
            String rowName = URIUtil.decodeUriPart(rowId);
            String colName = URIUtil.decodeUriPart(colId);
            JsonArrayBuilder jsonCheckList = Json.createArrayBuilder();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT checkName FROM checks " +
                    "WHERE gridName=? AND rowName=? AND colName=? AND active=1");
            stmt.setString(1, gridName);
            stmt.setString(2, rowName);
            stmt.setString(3, colName);
            boolean isEmpty = true;
            ResultSet checks = stmt.executeQuery();
            while(checks.next()){
                isEmpty = false;
                jsonCheckList.add(Json.createObjectBuilder()
                                        .add("name", checks.getString(1))
                                        .add("uri", "/" +  
                                                uriInfo.getPath() + "/" +
                                                URIUtil.normalizeURIPart(checks.getString(1))));
            }
            if(isEmpty){
                netlogger.info(netLog.end("maddash.ResourceManager.getCell"));
                return null;
            }
            conn.close();
            json.add("checks", jsonCheckList);
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.ResourceManager.getCell", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        
        netlogger.info(netLog.end("maddash.ResourceManager.getCell"));
        return json.build();
    }
    
    /**
     * Returns a grid check
     * 
     * @param gridId the id of the grid
     * @param rowId the id of the row
     * @param colId the id of the column
     * @param checkId the id of the check
     * @param uriInfo the URI used to request this object
     * @return a JSON representation of the row
     */
    public JsonObject getCheck(String gridId, String rowId, String colId, 
            String checkId, UriInfo uriInfo, int pageResults, int page) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder checkJson = Json.createObjectBuilder();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getCheck"));
            String gridName = URIUtil.decodeUriPart(gridId);
            String rowName = URIUtil.decodeUriPart(rowId);
            String colName = URIUtil.decodeUriPart(colId);
            String checkName = URIUtil.decodeUriPart(checkId);
            JsonArrayBuilder historyJson = Json.createArrayBuilder();
            int status = 0;
            
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT c.id, c.description, " +
                "c.prevCheckTime, c.nextCheckTime, c.checkStatus, c.prevResultCode," +
                " c.statusMessage, c.resultCount, c.active, " +
                "t.checkType, t.checkParams, t.checkInterval, t.retryInterval, " +
                "t.retryAttempts, t.timeout FROM checks AS c, checkTemplates " +
                "as t WHERE c.checkTemplateId=t.id AND gridName=? AND rowName=? AND " +
                "colName=? AND checkName=?");
            stmt.setString(1, gridName);
            stmt.setString(2, rowName);
            stmt.setString(3, colName);
            stmt.setString(4, checkName);
            ResultSet checkDetails = stmt.executeQuery();
            boolean isActive = false;
            ArrayList<Integer> checkIds = new ArrayList<Integer>();
            while(checkDetails.next()){
                //TODO handle changing of parameters over time better
                /* this will populate check details with active settings
                 * if there is an active test. if there is not it will have 
                 * the details of the last row found. Ids are all collected for history. 
                 */
                checkIds.add(checkDetails.getInt(1));
                if(isActive){
                    continue;
                }
                
                checkJson.add("gridName", gridName);
                checkJson.add("rowName", rowName);
                checkJson.add("colName", colName);
                checkJson.add("checkName", checkName);
                checkJson.add("description", checkDetails.getString(2));
                checkJson.add("prevCheckTime", checkDetails.getLong(3));
                checkJson.add("nextCheckTime", checkDetails.getLong(4));
                //need this later
                status = checkDetails.getInt(5);
                checkJson.add("status", status);
                checkJson.add("returnCode", checkDetails.getInt(6));
                checkJson.add("message", checkDetails.getString(7));
                checkJson.add("returnCodeCount", checkDetails.getInt(8));
                checkJson.add("type", checkDetails.getString(10));
                JsonObject paramObject = null;
                if(checkDetails.getString(11) != null && 
                        !checkDetails.getString(11).equals(CheckConstants.EMPTY_PARAMS)){
                    paramObject = Json.createReader(new StringReader(checkDetails.getString(11))).readObject();
                }
                checkJson.add("params", paramObject);
                checkJson.add("checkInterval", checkDetails.getInt(12));
                checkJson.add("retryInterval", checkDetails.getInt(13));
                checkJson.add("retryAttempts", checkDetails.getInt(14));

                if(checkDetails.getInt(9) == 1){
                    isActive=true;
                }
            }
            //verify check was found
            if(checkIds.size() == 0){
                netlogger.info(netLog.end("maddash.ResourceManager.getCheck"));
                return null;
            }
            
            //get custom states
            if(status < CheckConstants.RESULT_SHORT_NAMES.length){
                checkJson.add("statusShortName", CheckConstants.RESULT_SHORT_NAMES[status]);
            }else{
                PreparedStatement selCheckStateDefsStmt = conn.prepareStatement("SELECT shortName FROM checkStateDefs WHERE gridName=? AND stateValue=?");
                selCheckStateDefsStmt.setString(1, gridName);
                selCheckStateDefsStmt.setInt(2, status);
                ResultSet selCheckStateDefsResult = selCheckStateDefsStmt.executeQuery();
                if(selCheckStateDefsResult.next()){
                    checkJson.add("statusShortName", selCheckStateDefsResult.getString(1));
                }else{
                    checkJson.add("statusShortName", "CUSTOM");
                }
            }
            
            //get history
            String pageCountSql = "SELECT COUNT(*) FROM results WHERE checkId IN (";
            String historySql = "SELECT checkTime, returnCode, returnMessage, " +
                "returnParams, resultCount, checkStatus FROM results WHERE checkId IN ( ";
            boolean first = true;
            for(int checkDBId : checkIds){
                if(!first){
                    historySql += ", ";
                    pageCountSql += ", ";;
                }else{
                    first = false;
                }
                historySql += checkDBId;
                pageCountSql += checkDBId;
            }
            historySql += ") ORDER BY checkTime DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            pageCountSql += ")";
            if(pageResults <= 0){
                throw new RuntimeException("numResults parameter must be greater than 0");
            }
            if(page < 0){
                throw new RuntimeException("The page parameter must be greater than or equal to 0");
            }
            log.debug(historySql);
            
            //get page count
            ResultSet pageCountResults = conn.prepareStatement(pageCountSql).executeQuery();
            if(pageCountResults.next()){
               int rowCount = pageCountResults.getInt(1);
               int pageCount = rowCount/pageResults + ((rowCount%pageResults) == 0 ? 0 : 1);
               checkJson.add("historyPageCount", pageCount);
            }
            checkJson.add("historyResultPerPage", pageResults);
            
            PreparedStatement historyStmt = conn.prepareStatement(historySql);
            historyStmt.setInt(1, (pageResults * page));
            historyStmt.setInt(2, pageResults);
            ResultSet historyResults = historyStmt.executeQuery();
            while(historyResults.next()){
                JsonObjectBuilder resultJson = Json.createObjectBuilder();
                resultJson.add("time", historyResults.getLong(1));
                resultJson.add("returnCode", historyResults.getInt(2));
                resultJson.add("message", historyResults.getString(3));
                JsonObject jsonParams = null;
                if(historyResults.getString(4) != null && 
                        !historyResults.equals(CheckConstants.EMPTY_PARAMS)){
                    jsonParams = Json.createReader(new StringReader(historyResults.getString(4))).readObject();
                }
                resultJson.add("returnParams", jsonParams);
                resultJson.add("returnCodeCount", historyResults.getInt(5));
                resultJson.add("status", historyResults.getInt(6));
                historyJson.add(resultJson);
            }
            //output result
            checkJson.add("history", historyJson);
            
            conn.close();
        }catch(Exception e){
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.end("maddash.ResourceManager.getCheck"));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        
        netlogger.info(netLog.end("maddash.ResourceManager.getCheck"));
        return checkJson.build();
        
    }

    public JsonObject updateSchedule(JsonObject request) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder response = Json.createObjectBuilder();
        long nextCheckTime = 0;
        String sql = "UPDATE checks SET nextCheckTime=?";
        ArrayList<String> sqlParams = new ArrayList<String>();
        
        netlogger.info(netLog.start("maddash.ResourceManager.updateSchedule"));
        try{
            //check required fields
            JsonObject jsonCheckFilters = (JsonObject) RESTUtil.checkField(AdminScheduleResource.FIELD_CHECKFILTERS, request, true, false, null);
            nextCheckTime = RESTUtil.checkLongField(AdminScheduleResource.FIELD_NEXTCHECKTIME, request, true, true);
            //append where clause
            sql = RESTUtil.buildWhereClauseFromPost(sql, jsonCheckFilters, sqlParams);
            
            //Run update
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, nextCheckTime);
            for (int i= 0; i < sqlParams.size(); i++){
                stmt.setString(i+2, sqlParams.get(i));
            }
            int rowCount = stmt.executeUpdate();
            
            //build JSON response
            if(rowCount == 0){
                response.add("status", -1);
                response.add("checkUpdateCount", rowCount);
                response.add("message", "No checks matched request");
            }else{
                response.add("status", 0);
                response.add("checkUpdateCount", rowCount);
                response.add("message", "Successfully updated " + rowCount + " checks.");
            }
            conn.close();
        }catch(Exception e){
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.end("maddash.ResourceManager.updateSchedule"));
            log.error("Error handling request: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        
        netlogger.info(netLog.end("maddash.ResourceManager.updateSchedule"));
        return response.build();
    }

    public JsonObject createEvent(JsonObject request, UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder response = Json.createObjectBuilder();
        String selectSQL = "SELECT id FROM checks WHERE active=?";
        String insertSQL = "INSERT INTO events VALUES(DEFAULT, ?, ?, ?, ?, ?)";
        ArrayList<String> selectSQlParams = new ArrayList<String>();
        selectSQlParams.add("1");
        
        netlogger.info(netLog.start("maddash.ResourceManager.createEvent"));
        try{
            //check required fields
            JsonObject jsonCheckFilters = (JsonObject) RESTUtil.checkField(AdminScheduleResource.FIELD_CHECKFILTERS, request, true, false, null);
            selectSQL = RESTUtil.buildWhereClauseFromPost(selectSQL, jsonCheckFilters, selectSQlParams);
            
            //Run query to get list of affected checks
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(selectSQL);
            for (int i= 0; i < selectSQlParams.size(); i++){
                stmt.setString(i+1, selectSQlParams.get(i));
            }
            ResultSet matchingChecks = stmt.executeQuery();
            int matchingCheckCount = 0;
            PreparedStatement insertEventCheckStmt = conn.prepareStatement("INSERT INTO eventChecks VALUES(DEFAULT, ?, ?)");
            while(matchingChecks.next()){
                //Insert event if we have to
                if(matchingCheckCount == 0){
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
                    insertStmt.setString(1, RESTUtil.checkStringField(AdminEventsResource.FIELD_NAME, request, true, false));
                    insertStmt.setString(2, RESTUtil.checkStringField(AdminEventsResource.FIELD_DESCR, request, true, false));
                    insertStmt.setLong(3, RESTUtil.checkLongField(AdminEventsResource.FIELD_STARTTIME, request, true, false));
                    insertStmt.setLong(4, RESTUtil.checkLongField(AdminEventsResource.FIELD_ENDTIME, request, false, true));
                    insertStmt.setInt(5, RESTUtil.checkBooleanField(AdminEventsResource.FIELD_CHANGESTATUS, request, false, false, false));
                    insertStmt.executeUpdate();
                    ResultSet genKeys = insertStmt.getGeneratedKeys();
                    if(!genKeys.next()){
                        throw new RuntimeException("Unable to insert new event into database");
                    }
                    insertEventCheckStmt.setInt(1, genKeys.getInt(1));
                    response.add("uri", "/" + uriInfo.getPath() + "/" + genKeys.getInt(1));
                }
                matchingCheckCount++;
                
                //add event to check mapping
                insertEventCheckStmt.setInt(2, matchingChecks.getInt(1));
                insertEventCheckStmt.execute();
            }
            
            //throw error if no checks matched
            if(matchingCheckCount == 0){
                throw new RuntimeException("No checks matched search filter. Event was not created.");
            }
            
            conn.close();
        }catch(Exception e){
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.end("maddash.ResourceManager.createEvent"));
            log.error("Error handling request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        
        netlogger.info(netLog.end("maddash.ResourceManager.createEvent"));
        return response.build();
    }

    public JsonObject getEvents(List<String> gridName, List<String> rowName,
            List<String> colName, List<String> checkName, List<String> dimensionName, UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder response = Json.createObjectBuilder();
        String selectSQL = "SELECT DISTINCT events.id, events.name, events.description, events.startTime, events.endTime, events.changeStatus FROM events";
        
        netlogger.info(netLog.start("maddash.ResourceManager.getEvents"));
        try{
            ArrayList<String> sqlParams = new ArrayList<String>();
            String whereClause = RESTUtil.buildWhereClauseFromGet(gridName, rowName, colName, checkName, dimensionName, sqlParams);
            
            if(!"".equals(whereClause)){
                selectSQL += " INNER JOIN eventChecks ON events.id = eventChecks.eventId INNER JOIN checks ON checks.id=eventChecks.checkId";
                selectSQL += " WHERE " + whereClause;
            }
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(selectSQL);
            for(int i = 0; i < sqlParams.size(); i++){
                stmt.setString(i+1, sqlParams.get(i));
            }
            ResultSet queryResults = stmt.executeQuery();
            JsonArrayBuilder eventList = Json.createArrayBuilder();
            while(queryResults.next()){
                eventList.add(
                        Json.createObjectBuilder()
                            .add("uri", "/" + uriInfo.getPath() + "/" + queryResults.getInt(1))
                            .add("name", queryResults.getString(2))
                            .add("description", queryResults.getString(3))
                            .add("startTime", queryResults.getLong(4))
                            .add("endTime", queryResults.getLong(5))
                            .add("changeStatus", queryResults.getBoolean(6))
                        );
            }
            response.add("events", eventList);
            conn.close();
        }catch(Exception e){
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.end("maddash.ResourceManager.getEvents"));
            log.error("Error handling request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        
        netlogger.info(netLog.end("maddash.ResourceManager.getEvents"));
        return response.build();
    }

    public JsonObject getEvent(int eventId, UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder response = Json.createObjectBuilder();
        String eventSQL = "SELECT name, description, startTime, endTime, changeStatus FROM events WHERE id=?"; 
        String checkSQL = "SELECT checks.gridName, checks.rowName, checks.colName, checks.checkName " +
                            "FROM checks INNER JOIN eventChecks ON eventChecks.checkId = checks.id " +
                            "WHERE eventChecks.eventId=?"; 
        netlogger.info(netLog.start("maddash.ResourceManager.getEvent"));
        try{
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            
            //get the event
            PreparedStatement eventStmt = conn.prepareStatement(eventSQL);
            eventStmt.setInt(1, eventId);
            ResultSet eventResults = eventStmt.executeQuery();
            if(eventResults.next()){
                response.add("uri", "/" + uriInfo.getPath());
                response.add("name", eventResults.getString(1));
                response.add("description", eventResults.getString(2));
                response.add("startTime", eventResults.getLong(3));
                response.add("endTime", eventResults.getLong(4));
                response.add("changeStatus", eventResults.getBoolean(5));
            }else{
                return null;
            }
            
            //get the checks
            JsonArrayBuilder checkList =Json.createArrayBuilder();
            PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
            checkStmt.setInt(1, eventId);
            ResultSet checkResults = checkStmt.executeQuery();
            while(checkResults.next()){
                checkList.add(CheckResource.rootPath + //i know this isn't pretty
                        "/" + URIUtil.normalizeURIPart(checkResults.getString(1)) +
                        "/" + URIUtil.normalizeURIPart(checkResults.getString(2)) +
                        "/" + URIUtil.normalizeURIPart(checkResults.getString(3)) +
                        "/" + URIUtil.normalizeURIPart(checkResults.getString(4)));
            }
            response.add("checks", checkList);
            conn.close();
        }catch(Exception e){
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.end("maddash.ResourceManager.getEvent"));
            log.error("Error handling request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        netlogger.info(netLog.end("maddash.ResourceManager.getEvent"));
        return response.build();
    }
    
    public JsonObject deleteEvent(int eventId) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JsonObjectBuilder response = Json.createObjectBuilder();
        String eventChecksSQL = "DELETE FROM eventChecks WHERE eventId=?"; 
        String eventSQL = "DELETE FROM events WHERE id=?"; 
        
        netlogger.info(netLog.start("maddash.ResourceManager.deleteEvent"));
        try{
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            
            //delete the event
            PreparedStatement eventStmt = conn.prepareStatement(eventSQL);
            eventStmt.setInt(1, eventId);
            int rowCount = eventStmt.executeUpdate();
            //build JSON response
            if(rowCount == 0){
                return null;
            }else{
                response.add("status", 0);
                response.add("message", "Successfully deleted event");
            }
            
            //delete the eventChecks
            PreparedStatement eventChecksStmt = conn.prepareStatement(eventChecksSQL);
            eventChecksStmt.setInt(1, eventId);
            eventChecksStmt.executeUpdate();
            conn.close();
        }catch(Exception e){
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.end("maddash.ResourceManager.deleteEvent"));
            log.error("Error handling request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        netlogger.info(netLog.end("maddash.ResourceManager.deleteEvent"));
        return response.build();
    }
    
}

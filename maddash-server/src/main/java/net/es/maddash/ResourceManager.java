package net.es.maddash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;


import org.apache.log4j.Logger;

import net.es.maddash.checks.CheckConstants;
import net.es.maddash.utils.URIUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
    public JSONObject getDashboards() {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.ResourceManager.getDashboards"));
        JSONObject json = new JSONObject();
        json.put("dashboards", MaDDashGlobals.getInstance().getDashboards());
        netlogger.info(netLog.end("maddash.ResourceManager.getDashboards"));
        return json;
    }
    
    /**
     * Returns a list of grids 
     * 
     * @return List of grids as JSON
     */
    public JSONObject getGrids(UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getGrids"));
            JSONObject json = new JSONObject();
            JSONArray jsonGridList = new JSONArray();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet grids = stmt.executeQuery("SELECT DISTINCT gridName FROM checks");
            while(grids.next()){
                JSONObject gridJson = new JSONObject();
                gridJson.put("name", grids.getString(1));
                gridJson.put("uri", "/" + uriInfo.getPath() + "/" + URIUtil.normalizeURIPart(grids.getString(1)));
                jsonGridList.add(gridJson);
            }
            conn.close(); 
            json.put("grids", jsonGridList);
            netlogger.info(netLog.end("maddash.ResourceManager.getGrids"));
            return json;
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
     * Returns the full grid specified.
     * 
     * @param gridId the ID of the grid to query
     */
    public JSONObject getGrid(String gridId, UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.ResourceManager.getGrid"));
        JSONObject json = new JSONObject();
        try {
            String gridName = URIUtil.decodeUriPart(gridId);
            ArrayList<JSONObject> rowList = new ArrayList<JSONObject>();
            json.put("name", gridName);
            
            //query database
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            
            //get grid-wide information
            PreparedStatement selGridStmt = conn.prepareStatement("SELECT okLabel, warningLabel, " +
            		"criticalLabel, unknownLabel, notRunLabel FROM grids WHERE gridName=?");
            selGridStmt.setString(1, gridName);
            ResultSet gridResult = selGridStmt.executeQuery();
            if(gridResult.next()){
                JSONArray statusLabels = new JSONArray();
                statusLabels.add("".equals(gridResult.getString(1)) ? null : gridResult.getString(1));
                statusLabels.add("".equals(gridResult.getString(2)) ? null : gridResult.getString(2));
                statusLabels.add("".equals(gridResult.getString(3)) ? null : gridResult.getString(3));
                statusLabels.add("".equals(gridResult.getString(4)) ? null : gridResult.getString(4));
                statusLabels.add("".equals(gridResult.getString(5)) ? null : gridResult.getString(5));
                json.put("statusLabels", statusLabels);
            }else{
                //grid not found
                return null;
            }
            
            
            //get information on checks
            PreparedStatement stmt = conn.prepareStatement("SELECT rowName, colName," +
                " checkName, colOrder, statusMessage, checkStatus, prevCheckTime FROM checks WHERE gridName=? " +
                "AND active=1 ORDER BY rowOrder, colOrder, checkName");
            stmt.setString(1, gridName);
            ResultSet results = stmt.executeQuery();
            
            HashMap<Integer,String> colMap = new HashMap<Integer,String>();
            ArrayList<String> colList = new ArrayList<String>();
            HashMap<String,Boolean> checkMap = new HashMap<String,Boolean>();
            HashMap<String,Map<String, Map<String,JSONObject>>> grid = new HashMap<String,Map<String, Map<String,JSONObject>>>();
            String lastRow = null;
            String lastCol = null;
            HashMap<String,Map<String,JSONObject>> curRowCols = null;
            HashMap<String,JSONObject> curColChecks = null; 
            Long lastUpdateTime = null;
            while(results.next()){
                if(lastUpdateTime == null){
                    lastUpdateTime = results.getLong(7);
                }else if(results.getLong(7) > 0 && results.getLong(7) > lastUpdateTime){
                    lastUpdateTime = results.getLong(7);
                    
                }
                
                String rowName = results.getString(1);
                if(!rowName.equals(lastRow)){
                    JSONObject tmpRowObj = new JSONObject();
                    tmpRowObj.put("name", rowName);
                    tmpRowObj.put("uri", "/" + uriInfo.getPath() + 
                            "/" + URIUtil.normalizeURIPart(rowName));
                    rowList.add(tmpRowObj);
                    curRowCols = new HashMap<String,Map<String,JSONObject>>();
                    grid.put(rowName, curRowCols);
                    lastRow = rowName;
                    lastCol = null;
                }
                String colName = results.getString(2);
                if(!colName.equals(lastCol)){
                    curColChecks = new HashMap<String,JSONObject>();
                    curRowCols.put(colName, curColChecks);
                    lastCol = colName;
                }
                colMap.put(results.getInt(4), colName);
                
                String checkName = results.getString(3);
                JSONObject curCheckMap = new JSONObject();
                //curCheckMap.put("name", results.getString(3));
                curCheckMap.put("message", results.getString(5));
                curCheckMap.put("status", results.getInt(6));
                curCheckMap.put("prevCheckTime", results.getLong(7));
                curCheckMap.put("uri", "/" + uriInfo.getPath() +
                        "/" + URIUtil.normalizeURIPart(rowName) + "/" +
                        URIUtil.normalizeURIPart(colName) + "/" + URIUtil.normalizeURIPart(checkName));
                curColChecks.put(checkName, curCheckMap);
                checkMap.put(checkName, true);
            }
            conn.close();
            
            //build json
            ArrayList<Integer> checkOrderList = new ArrayList<Integer>(colMap.keySet());
            Collections.sort(checkOrderList);
            for(Integer colIndex : checkOrderList){
                colList.add(colMap.get(colIndex));
            }
            
            ArrayList<String> checkList = new ArrayList<String>(checkMap.keySet());
            Collections.sort(checkList);
            json.put("lastUpdateTime", lastUpdateTime);
            json.put("rows", rowList);
            json.put("columnNames", colList);
            json.put("checkNames", checkList);
            JSONArray jsonGrid = new JSONArray();
            for(JSONObject rowObj : rowList){
                String row = rowObj.getString("name");
                JSONArray jsonRow = new JSONArray();
                for(String col: colList){
                    if(!grid.get(row).containsKey(col)){
                        jsonRow.add(null);
                        continue;
                    }
                    JSONArray jsonCol = new JSONArray();
                    for(String check : checkList){
                        if(grid.get(row).get(col).containsKey(check)){
                            jsonCol.add(grid.get(row).get(col).get(check));
                        }else{
                            jsonCol.add(null);
                        }
                    }
                    jsonRow.add(jsonCol);
                }
                jsonGrid.add(jsonRow);
            }
            json.put("grid", jsonGrid);
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.ResourceManager.getGrid", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        
        netlogger.info(netLog.end("maddash.ResourceManager.getGrid"));
        return json;
    }

    /**
     * Returns a grid row
     * 
     * @param gridId the id of the grid
     * @param rowId the id of the row
     * @param uriInfo the URI used to request this object
     * @return a JSON representation of the row
     */
    public JSONObject getRow(String gridId, String rowId, UriInfo uriInfo){
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JSONObject json = null;
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getRow"));
            String gridName = URIUtil.decodeUriPart(gridId);
            String rowName = URIUtil.decodeUriPart(rowId);
            json = new JSONObject();
            JSONArray jsonColList = new JSONArray();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT colName FROM checks " +
                    "WHERE gridName=? AND rowName=? AND active=1");
            stmt.setString(1, gridName);
            stmt.setString(2, rowName);
            ResultSet cols = stmt.executeQuery();
            while(cols.next()){
                JSONObject colJson = new JSONObject();
                colJson.put("name", cols.getString(1));
                colJson.put("uri", "/" + uriInfo.getPath() + "/" +
                        URIUtil.normalizeURIPart(cols.getString(1)));
                jsonColList.add(colJson);
            }
            //no row found
            if(jsonColList.size() == 0){
                netlogger.info(netLog.end("maddash.ResourceManager.getRow"));
                return null;
            }
            conn.close();
            json.put("cells", jsonColList);
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
        return json;
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
    public JSONObject getCell(String gridId, String rowId, String colId, UriInfo uriInfo) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JSONObject json = new JSONObject();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getCell"));
            String gridName = URIUtil.decodeUriPart(gridId);
            String rowName = URIUtil.decodeUriPart(rowId);
            String colName = URIUtil.decodeUriPart(colId);
            JSONArray jsonCheckList = new JSONArray();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT checkName FROM checks " +
                    "WHERE gridName=? AND rowName=? AND colName=? AND active=1");
            stmt.setString(1, gridName);
            stmt.setString(2, rowName);
            stmt.setString(3, colName);
            ResultSet checks = stmt.executeQuery();
            while(checks.next()){
                JSONObject checkJson = new JSONObject();
                checkJson.put("name", checks.getString(1));
                checkJson.put("uri", "/" +  
                        uriInfo.getPath() + "/" +
                        URIUtil.normalizeURIPart(checks.getString(1)));
                jsonCheckList.add(checkJson);
            }
            if(jsonCheckList.size() == 0){
                netlogger.info(netLog.end("maddash.ResourceManager.getCell"));
                return null;
            }
            conn.close();
            json.put("checks", jsonCheckList);
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
        return json;
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
    public JSONObject getCheck(String gridId, String rowId, String colId, 
            String checkId, UriInfo uriInfo, int pageResults, int page) {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        JSONObject checkJson = new JSONObject();
        try {
            netlogger.info(netLog.start("maddash.ResourceManager.getCheck"));
            String gridName = URIUtil.decodeUriPart(gridId);
            String rowName = URIUtil.decodeUriPart(rowId);
            String colName = URIUtil.decodeUriPart(colId);
            String checkName = URIUtil.decodeUriPart(checkId);
            JSONArray historyJson = new JSONArray();
            
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
                
                checkJson.put("gridName", gridName);
                checkJson.put("rowName", rowName);
                checkJson.put("colName", colName);
                checkJson.put("checkName", checkName);
                checkJson.put("description", checkDetails.getString(2));
                checkJson.put("prevCheckTime", checkDetails.getLong(3));
                checkJson.put("nextCheckTime", checkDetails.getLong(4));
                checkJson.put("status", checkDetails.getInt(5));
                checkJson.put("returnCode", checkDetails.getInt(6));
                checkJson.put("message", checkDetails.getString(7));
                checkJson.put("returnCodeCount", checkDetails.getInt(8));
                checkJson.put("type", checkDetails.getString(10));
                JSONObject paramObject = null;
                if(checkDetails.getString(11) != null && 
                        !checkDetails.getString(11).equals(CheckConstants.EMPTY_PARAMS)){
                    paramObject = JSONObject.fromObject(checkDetails.getString(11));
                }
                checkJson.put("params", paramObject);
                checkJson.put("checkInterval", checkDetails.getInt(12));
                checkJson.put("retryInterval", checkDetails.getInt(13));
                checkJson.put("retryAttempts", checkDetails.getInt(14));

                if(checkDetails.getInt(9) == 1){
                    isActive=true;
                }
            }
            //verify check was found
            if(checkIds.size() == 0){
                netlogger.info(netLog.end("maddash.ResourceManager.getCheck"));
                return null;
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
               checkJson.put("historyPageCount", pageCount);
            }
            checkJson.put("historyResultPerPage", pageResults);
            
            PreparedStatement historyStmt = conn.prepareStatement(historySql);
            historyStmt.setInt(1, (pageResults * page));
            historyStmt.setInt(2, pageResults);
            ResultSet historyResults = historyStmt.executeQuery();
            while(historyResults.next()){
                JSONObject resultJson = new JSONObject();
                resultJson.put("time", historyResults.getLong(1));
                resultJson.put("returnCode", historyResults.getInt(2));
                resultJson.put("message", historyResults.getString(3));
                JSONObject jsonParams = null;
                if(historyResults.getString(4) != null && 
                        !historyResults.equals(CheckConstants.EMPTY_PARAMS)){
                    jsonParams = JSONObject.fromObject(historyResults.getString(4));
                }
                resultJson.put("returnParams", jsonParams);
                resultJson.put("returnCodeCount", historyResults.getInt(5));
                resultJson.put("status", historyResults.getInt(6));
                historyJson.add(resultJson);
            }
            conn.close();
            
            //output result
            checkJson.put("history", historyJson);
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
        return checkJson;
        
    }
    
}

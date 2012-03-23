package net.es.maddash;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.es.maddash.checks.CheckConstants;
import net.es.maddash.checks.NagiosCheck;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

public class MaDDashHTTPHandler extends AbstractHandler{
    private Logger log = Logger.getLogger(NagiosCheck.class);
    private Logger netlogger = Logger.getLogger("netlogger");
    
    private String rootPath;
    final private int DEFAULT_RESULT_LIMIT = 10;
    
    
    public MaDDashHTTPHandler(String rootPath){
        super();
        this.rootPath = rootPath;
    }
    
    public void handle(String target, HttpServletRequest request,
            HttpServletResponse response, int dispatch) throws IOException,
            ServletException {

        //handle method
        ((Request)request).setHandled(true);
        if(request.getMethod().equals("GET")){
            this.getResource(target, request, response);
        }else {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", "GET");
            response.getWriter().write("<h1>405 Method Not Allowed</h1>");
            return;
        }
        
    }

    private void getResource(String target, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if(!target.startsWith(rootPath)){
            this.reportNotFound(response);
            return;
        }
        
        String[] resourcePath = target.replaceFirst("/", "").split("/");
        if(resourcePath.length == 1){
            this.getGridList(resourcePath, request, response);
        }else if(resourcePath.length == 2){
            this.getGrid(resourcePath, request, response);
        }else if(resourcePath.length == 3){
            this.getGridRow(resourcePath, request, response);
        }else if(resourcePath.length == 4){
            this.getGridCol(resourcePath, request, response);
        }else if(resourcePath.length == 5){
            this.getCheckResult(resourcePath, request, response);
        }else{
            this.reportNotFound(response);
            return;
        }
    }

    private void getCheckResult(String[] resourcePath,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.http.getCheckResult"));
            String gridName = this.decodeUriPart(resourcePath[1]);
            String rowName = this.decodeUriPart(resourcePath[2]);
            String colName = this.decodeUriPart(resourcePath[3]);
            String checkName = this.decodeUriPart(resourcePath[4]);
            JSONObject checkJson = new JSONObject();
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
                checkJson.put("resultCount", checkDetails.getInt(8));
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
            if(!checkJson.containsKey("status") || checkJson.get("status") == null){
                this.reportNotFound(response);
            }
            
            //get history
            String pageCountSql = "SELECT COUNT(*) FROM results WHERE checkId IN (";
            String historySql = "SELECT checkTime, returnCode, returnMessage, " +
                "returnParams, resultCount, checkStatus FROM results WHERE checkId IN ( ";
            boolean first = true;
            for(int checkId : checkIds){
                if(!first){
                    historySql += ", ";
                    pageCountSql += ", ";;
                }else{
                    first = false;
                }
                historySql += checkId;
                pageCountSql += checkId;
            }
            historySql += ") ORDER BY checkTime DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            pageCountSql += ")";
            String numResultsStr = request.getParameter("numResults");
            int numResults = DEFAULT_RESULT_LIMIT;
            if(numResultsStr != null){
                numResults = Integer.parseInt(numResultsStr);
            }
            if(numResults <= 0){
                //prevent divide by 0
                numResults = DEFAULT_RESULT_LIMIT;
            }
            String pageStr = request.getParameter("page");
            int page = 0;
            if(pageStr != null){
                page = Integer.parseInt(pageStr);
            }
            log.debug(historySql);
            
            //get page count
            ResultSet pageCountResults = conn.prepareStatement(pageCountSql).executeQuery();
            if(pageCountResults.next()){
               int rowCount = pageCountResults.getInt(1);
               int pageCount = rowCount/numResults + ((rowCount%numResults) == 0 ? 0 : 1);
               checkJson.put("historyPageCount", pageCount);
            }
            
            PreparedStatement historyStmt = conn.prepareStatement(historySql);
            historyStmt.setInt(1, (numResults * page));
            historyStmt.setInt(2, numResults);
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
                resultJson.put("count", historyResults.getInt(5));
                resultJson.put("status", historyResults.getInt(6));
                historyJson.add(resultJson);
            }
            conn.close();
            
            //output result
            checkJson.put("history", historyJson);
            this.outputJSON(response, checkJson);
            netlogger.info(netLog.end("maddash.http.getCheckResult"));
        }catch(Exception e){
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.end("maddash.http.getCheckResult"));
            log.error("Error handling request: " + e.getMessage());
            this.reportError(response, e.getMessage());
            e.printStackTrace();
        }
        
    }

    private void getGridCol(String[] resourcePath, HttpServletRequest request,
            HttpServletResponse response) {
        // TODO Auto-generated method stub
        
    }

    private void getGridRow(String[] resourcePath, HttpServletRequest request,
            HttpServletResponse response) {
        
    }

    private void getGrid(String[] resourcePath, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.http.getGrid"));
            String gridName = this.decodeUriPart(resourcePath[1]);
            JSONObject json = new JSONObject();
            ArrayList<JSONObject> rowList = new ArrayList<JSONObject>();
            
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT rowName, colName," +
                " checkName, colOrder, statusMessage, checkStatus FROM checks WHERE gridName=? " +
                "AND active=1 ORDER BY rowOrder, colOrder, checkName");
            stmt.setString(1, gridName);
            ResultSet results = stmt.executeQuery();
            HashMap<Integer,String> colMap = new HashMap<Integer,String>();
            ArrayList<String> colList = new ArrayList<String>();
            HashMap<String,Boolean> checkMap = new HashMap<String,Boolean>();
            HashMap<String,Map<String, Map<String,Map<String, String>>>> grid = new HashMap<String,Map<String, Map<String,Map<String, String>>>>();
            String lastRow = null;
            String lastCol = null;
            HashMap<String,Map<String,Map<String,String>>> curRowCols = null;
            HashMap<String,Map<String,String>> curColChecks = null; 
            while(results.next()){
                String rowName = results.getString(1);
                if(!rowName.equals(lastRow)){
                    JSONObject tmpRowObj = new JSONObject();
                    tmpRowObj.put("name", rowName);
                    tmpRowObj.put("uri", rootPath + "/" + this.normalizeURIPart(gridName) + 
                            "/" + this.normalizeURIPart(rowName));
                    rowList.add(tmpRowObj);
                    curRowCols = new HashMap<String,Map<String,Map<String,String>>>();
                    grid.put(rowName, curRowCols);
                    lastRow = rowName;
                    lastCol = null;
                }
                String colName = results.getString(2);
                if(!colName.equals(lastCol)){
                    curColChecks = new HashMap<String,Map<String,String>>();
                    curRowCols.put(colName, curColChecks);
                    lastCol = colName;
                }
                colMap.put(results.getInt(4), colName);
                
                String checkName = results.getString(3);
                HashMap<String,String> curCheckMap = new HashMap<String,String>();
                //curCheckMap.put("name", results.getString(3));
                curCheckMap.put("message", results.getString(5));
                curCheckMap.put("status", results.getString(6));
                curCheckMap.put("uri", rootPath + "/" + this.normalizeURIPart(gridName) +
                        "/" + this.normalizeURIPart(rowName) + "/" +
                        this.normalizeURIPart(colName) + "/" + this.normalizeURIPart(checkName));
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
            json.put("name", gridName);
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
            
            this.outputJSON(response, json);
            netlogger.info(netLog.end("maddash.http.getGrid"));
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.http.getGrid", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            e.printStackTrace();
            this.reportError(response, e.getMessage());
        }
    }

    private void getGridList(String[] resourcePath, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        try {
            netlogger.info(netLog.start("maddash.http.getGridList"));
            JSONObject json = new JSONObject();
            JSONArray jsonGridList = new JSONArray();
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet grids = stmt.executeQuery("SELECT DISTINCT gridName FROM checks");
            while(grids.next()){
                JSONObject gridJson = new JSONObject();
                gridJson.put("name", grids.getString(1));
                gridJson.put("uri", rootPath + "/" + this.normalizeURIPart(grids.getString(1)));
                jsonGridList.add(gridJson);
            }
            conn.close();
            
            json.put("title", MaDDashGlobals.getInstance().getWebTitle());
            json.put("defaultDashboard", MaDDashGlobals.getInstance().getDefaultDashboard());
            json.put("dashboards", MaDDashGlobals.getInstance().getDashboards());
            json.put("grids", jsonGridList);
            this.outputJSON(response, json);
            netlogger.info(netLog.end("maddash.http.getGridList"));
        } catch (Exception e) {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
            netlogger.error(netLog.error("maddash.http.getGridList", e.getMessage()));
            log.error("Error handling request: " + e.getMessage());
            this.reportError(response, e.getMessage());
        }
    }

    private void outputJSON(HttpServletResponse response, JSONObject json) throws IOException {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.debug(netLog.start("maddash.http.outputJSON"));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.getWriter().write(json+"");
        netlogger.debug(netLog.end("maddash.http.outputJSON"));
    }

    private String normalizeURIPart(String uriPart) {
        try {
            uriPart = URLEncoder.encode(uriPart, "UTF-8");
        } catch (UnsupportedEncodingException e) {}
        return uriPart;
    }

    private String decodeUriPart(String uriPart) {
        try {
            uriPart = URLDecoder.decode(uriPart, "UTF-8");
        } catch (UnsupportedEncodingException e) {}
        return uriPart;
    }
    
    private void reportError(HttpServletResponse response, String message) throws IOException {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.debug(netLog.start("maddash.http.reportError"));
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("<h1>500 Internal Server Error</h1>");
        response.getWriter().write("<h2>" + message + "</h2");
        netlogger.debug(netLog.start("maddash.http.reportError"));
    }

    private void reportNotFound(HttpServletResponse response) throws IOException {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.debug(netLog.start("maddash.http.reportNotFound"));
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("<h1>404 Not Found</h1>");
        netlogger.debug(netLog.end("maddash.http.reportNotFound"));
    }
    
    /**
     * @return the settPath
     */
    public void setRootPath(String rootPath) {
        if(rootPath != null && !rootPath.startsWith("/")){
            rootPath = "/" + rootPath;
        }
        this.rootPath = rootPath;
    }
    
    /**
     * @return the rootPath
     */
    public String getRootPath() {
        return this.rootPath;
    }
    
}

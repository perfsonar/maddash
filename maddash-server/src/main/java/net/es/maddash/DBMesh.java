package net.es.maddash;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.log4j.Logger;

import net.es.maddash.madalert.BaseMesh;
import net.es.maddash.madalert.JsonUtil;
import net.es.maddash.utils.DimensionUtil;
import net.es.maddash.utils.URIUtil;

/**
 * A mesh that queries a Derby or compatible database using the JDBC and builds a JSON grid.
 * 
 * @author alake
 *
 */
public class DBMesh extends BaseMesh{
    private Logger log = Logger.getLogger(ResourceManager.class);
    private Logger netlogger = Logger.getLogger("netlogger");
    
    private String name;
    private JsonArray statusLabels;
    private Long lastUpdateTime;
    private JsonArray columnNames;
    private JsonArray columnProps;
    private JsonArray checkNames;
    private JsonArray grid;
    private JsonArray rows;
     
    public JsonObjectBuilder toJsonBuilder(){
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("name", this.name);
        json.add("statusLabels", this.statusLabels);
        json.add("lastUpdateTime", lastUpdateTime);
        json.add("columnNames", this.columnNames);
        json.add("columnProps", this.columnProps);
        json.add("checkNames", this.checkNames);
        json.add("grid", this.grid);
        json.add("rows", this.rows);
        return json;
    }
    
    public DBMesh(String gridName, String uriPath){
        this.name = gridName;
        Connection conn = null;
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.NativeMesh.init"));
        try {
            List<String> rowNames = new ArrayList<String>();
            List<JsonObjectBuilder> rowList = new ArrayList<JsonObjectBuilder>();
            
            //query database
            conn = MaDDashGlobals.getInstance().getDataSource().getConnection();
            
            //get grid-wide information
            PreparedStatement selGridStmt = conn.prepareStatement("SELECT okLabel, warningLabel, " +
                        "criticalLabel, unknownLabel, notRunLabel FROM grids WHERE gridName=?");
            selGridStmt.setString(1, gridName);
            ResultSet gridResult = selGridStmt.executeQuery();
            JsonArrayBuilder statusLabels = Json.createArrayBuilder();
            int lastLabelState = 0;
            if(gridResult.next()){
                this.addStringOrNull(statusLabels, gridResult.getString(1));
                this.addStringOrNull(statusLabels, gridResult.getString(2));
                this.addStringOrNull(statusLabels, gridResult.getString(3));
                this.addStringOrNull(statusLabels, gridResult.getString(4));
                this.addStringOrNull(statusLabels, gridResult.getString(5));
                //if increase states added above, increase this
                lastLabelState = 5;
            }else{
                //grid not found
                return;
            }
            
            //get custom states
            PreparedStatement selCheckStateDefsStmt = conn.prepareStatement("SELECT stateValue, description FROM checkStateDefs WHERE gridName=? ORDER BY stateValue ASC");
            selCheckStateDefsStmt.setString(1, gridName);
            ResultSet checkStateDefsResult = selCheckStateDefsStmt.executeQuery();
            if(checkStateDefsResult.next()){
                //add empty values until reach next state
                while(lastLabelState < checkStateDefsResult.getInt(1)){
                    statusLabels.add(JsonValue.NULL);
                    lastLabelState++;
                }
                statusLabels.add(checkStateDefsResult.getString(2));
                lastLabelState = checkStateDefsResult.getInt(1);
            }
            this.statusLabels = statusLabels.build();
            
            //get row and column labels
            HashMap<String, String> dimesnionLabelMap = new HashMap<String, String>();
            HashMap<String,JsonObjectBuilder> dimensionProperties = new HashMap<String,JsonObjectBuilder>();
            PreparedStatement labelStmt = conn.prepareStatement("SELECT configIdent, keyName, value FROM dimensions");
            ResultSet labelResults = labelStmt.executeQuery();
            while(labelResults.next()){
                if(ConfigLoader.PROP_DIMENSIONS_LABEL.equals(labelResults.getString(2))){
                    dimesnionLabelMap.put(labelResults.getString(1), labelResults.getString(3));
                }
                if(!dimensionProperties.containsKey(labelResults.getString(1)) || dimensionProperties.get(labelResults.getString(1)) == null){
                    dimensionProperties.put(labelResults.getString(1), Json.createObjectBuilder());
                }
                if(ConfigLoader.PROP_DIMENSION_MAP.equals(labelResults.getString(2))){
                    dimensionProperties.get(labelResults.getString(1)).add(labelResults.getString(2).toLowerCase(), Json.createReader(new StringReader(labelResults.getString(3))).readObject());
                }else{
                    dimensionProperties.get(labelResults.getString(1)).add(labelResults.getString(2).toLowerCase(), labelResults.getString(3));
                }
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
            HashMap<String,Map<String, Map<String,JsonObjectBuilder>>> grid = new HashMap<String,Map<String, Map<String,JsonObjectBuilder>>>();
            String lastRow = null;
            String lastCol = null;
            HashMap<String,Map<String,JsonObjectBuilder>> curRowCols = null;
            HashMap<String,JsonObjectBuilder> curColChecks = null;
            Long lastUpdateTime = null;
            while(results.next()){
                if(lastUpdateTime == null){
                    lastUpdateTime = results.getLong(7);
                }else if(results.getLong(7) > 0 && results.getLong(7) > lastUpdateTime){
                    lastUpdateTime = results.getLong(7);
                    
                }
                
                //translate to label of there is one
                String rowName = results.getString(1);
                if(!rowName.equals(lastRow)){
                    JsonObjectBuilder tmpRowObj = Json.createObjectBuilder();
                    tmpRowObj.add("name", rowName);
                    tmpRowObj.add("uri", "/" + uriPath + 
                            "/" + URIUtil.normalizeURIPart(rowName));
                    if(dimensionProperties.containsKey(rowName) && dimensionProperties.get(rowName) != null){
                        tmpRowObj.add("props", dimensionProperties.get(rowName));
                    }else{
                        tmpRowObj.add("props", Json.createObjectBuilder());
                    }
                    rowList.add(tmpRowObj);
                    rowNames.add(rowName);
                    curRowCols = new HashMap<String,Map<String,JsonObjectBuilder>>();
                    grid.put(rowName, curRowCols);
                    lastRow = rowName;
                    lastCol = null;
                }
                String colName = results.getString(2);
                if(!colName.equals(lastCol)){
                    curColChecks = new HashMap<String,JsonObjectBuilder>();
                    curRowCols.put(colName, curColChecks);
                    lastCol = colName;
                }
                colMap.put(results.getInt(4), colName);
                
                String checkName = results.getString(3);
                curColChecks.put(checkName, Json.createObjectBuilder()
                        .add("message", results.getString(5))
                        .add("status", results.getInt(6))
                        .add("prevCheckTime", results.getLong(7))
                        .add("uri", "/" + uriPath +
                                "/" + URIUtil.normalizeURIPart(rowName) + "/" +
                                URIUtil.normalizeURIPart(colName) + "/" + URIUtil.normalizeURIPart(checkName)));
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
            JsonArrayBuilder jsonCheckList = Json.createArrayBuilder();
            for(String checkListItem : checkList){
                jsonCheckList.add(checkListItem);
            }
            this.lastUpdateTime = lastUpdateTime;
            this.columnNames = DimensionUtil.translateNames(colList, dimesnionLabelMap);
            this.columnProps = DimensionUtil.translateProperties(colList, dimensionProperties);
            this.checkNames = jsonCheckList.build();
            
            JsonArrayBuilder jsonGrid = Json.createArrayBuilder();
            for(String row : rowNames){
                JsonArrayBuilder jsonRow = Json.createArrayBuilder();
                for(String col: colList){
                    if(!grid.get(row).containsKey(col)){
                        jsonRow.add(JsonValue.NULL);
                        continue;
                    }
                    JsonArrayBuilder jsonCol = Json.createArrayBuilder();
                    for(String check : checkList){
                        if(grid.get(row).get(col).containsKey(check)){
                            jsonCol.add(grid.get(row).get(col).get(check));
                        }else{
                            jsonCol.add(JsonValue.NULL);
                        }
                    }
                    jsonRow.add(jsonCol);
                }
                jsonGrid.add(jsonRow);
            }
            this.grid = jsonGrid.build();
            this.rows = DimensionUtil.translateJsonObjNames(rowNames, rowList, dimesnionLabelMap);
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
    }

    @Override
    public List<String> getColumnNames() {
        return JsonUtil.toListString(this.columnNames);
    }

    @Override
    public List<String> getRowNames() {
        ArrayList<String> rowNames = new ArrayList<String>();
        for(int i = 0; i < this.rows.size(); i++){
            rowNames.add(this.rows.getJsonObject(i).getJsonObject("props").getString("label"));
        }
        return rowNames;
    }

    @Override
    public int getCheckCount() {
        return this.checkNames.size();
    }

    @Override
    public List<String> getStatusLabels() {
        return JsonUtil.toListString(this.statusLabels);
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int statusFor(int row, int column, int check) {
        return this.grid.getJsonArray(row).getJsonArray(column).getJsonObject(check).getInt("status");
    }

    @Override
    public boolean hasColumn(int row, int column) {
        return this.grid.getJsonArray(row).get(column) != JsonValue.NULL;
    }
    
    private void addStringOrNull(JsonArrayBuilder list, String val) {
        if("".equals(val)){
            list.add(JsonValue.NULL);
        }else{
            list.add(val);
        }
        
    }

}

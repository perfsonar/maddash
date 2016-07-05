package net.es.maddash.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class RESTUtil {
    
    final public static String CHECKFILTERS_GRIDNAME = "gridName";
    final public static String CHECKFILTERS_ROWNAME = "rowName";
    final public static String CHECKFILTERS_COLNAME = "columnName";
    final public static String CHECKFILTERS_CHECKNAME = "checkName";
    final public static String CHECKFILTERS_DIMENSIONNAME = "dimensionName";
    
    static public HashMap<String, List<String>> createAdminFilterMap(){
        HashMap<String,List<String>> fields = new HashMap<String,List<String>>();
        fields.put(CHECKFILTERS_GRIDNAME, new ArrayList<String>());
        fields.put(CHECKFILTERS_ROWNAME, new ArrayList<String>());
        fields.put(CHECKFILTERS_COLNAME, new ArrayList<String>());
        fields.put(CHECKFILTERS_CHECKNAME, new ArrayList<String>());
        fields.put(CHECKFILTERS_COLNAME, new ArrayList<String>());
        fields.put(CHECKFILTERS_DIMENSIONNAME, new ArrayList<String>());
        
        //single column filters
        fields.get(CHECKFILTERS_GRIDNAME).add("gridName");
        fields.get(CHECKFILTERS_ROWNAME).add("rowName");
        fields.get(CHECKFILTERS_COLNAME).add("colName");
        fields.get(CHECKFILTERS_CHECKNAME).add("checkName");
        //compound filter
        fields.get(CHECKFILTERS_DIMENSIONNAME).add("rowName");
        fields.get(CHECKFILTERS_DIMENSIONNAME).add("colName");
        
        return fields;
    }
    
    static public Object checkField(String field, JsonObject request, boolean required, boolean nullable, String defaultVal){
        if(!request.containsKey(field)){
            if(required){
                throw new RuntimeException("Missing required field " + field);
            }else{
                return defaultVal;
            }
        }else if(request.isNull(field)){
            if(!nullable){
                throw new RuntimeException("Field " + field + " cannot be null");
            }
        }
        
        return request.get(field);
    }
    
    static public String checkStringField(String field, JsonObject request, boolean required, boolean nullable){
        if(!request.containsKey(field)){
            if(required){
                throw new RuntimeException("Missing required field " + field);
            }else{
                return null;
            }
        }else if(request.isNull(field)){
            if(!nullable){
                throw new RuntimeException("Field " + field + " cannot be null");
            }
        }
        
        return request.getString(field);
    }
    
    static public Long checkLongField(String field, JsonObject request, boolean required, boolean nullable){
        if(!request.containsKey(field)){
            if(required){
                throw new RuntimeException("Missing required field " + field);
            }else{
                return (long) -1;
            }
        }else if(request.isNull(field)){
            if(!nullable){
                throw new RuntimeException("Field " + field + " cannot be null");
            }else{
                return (long) -1;//return here because getLong does not return null
            }
        }
        
        return request.getJsonNumber(field).longValueExact();
    }
    
    public static int checkBooleanField(String field, JsonObject request, boolean required, boolean nullable, boolean defaultVal) {
        int defaultIntVal = defaultVal ? 1 : 0;
        if(!request.containsKey(field)){
            if(required){
                throw new RuntimeException("Missing required field " + field);
            }else{
                return defaultIntVal;
            }
        }else if(request.isNull(field)){
            if(!nullable){
                throw new RuntimeException("Field " + field + " cannot be null");
            }else{
                return defaultIntVal;
            }
        }
        
        return (request.getBoolean(field) ? 1 : 0);
    }
    
    static public String buildWhereClauseFromGet(List<String> gridName, List<String> rowName, List<String> colName, List<String> checkName, List<String> dimensionName, List<String> sqlParams){
        String whereClause = "";
        whereClause = RESTUtil.buildWhereClauseFromGet(whereClause, gridName, "checks.gridName", sqlParams);
        whereClause = RESTUtil.buildWhereClauseFromGet(whereClause, rowName, "checks.rowName", sqlParams);
        whereClause = RESTUtil.buildWhereClauseFromGet(whereClause, colName, "checks.colName", sqlParams);
        whereClause = RESTUtil.buildWhereClauseFromGet(whereClause, checkName, "checks.checkName", sqlParams);
        String[] dimensionCols = {"checks.rowName", "checks.colName"};
        whereClause = RESTUtil.buildWhereClauseFromGet(whereClause, dimensionName, dimensionCols, sqlParams);
        
        return whereClause;
    }
    
    static public String buildWhereClauseFromGet(String whereClause, List<String> params, String sqlColName, List<String> sqlParams){
        String[] tmp = {sqlColName};
        return RESTUtil.buildWhereClauseFromGet(whereClause, params, tmp, sqlParams);
    }
    
    static public String buildWhereClauseFromGet(String whereClause, List<String> params, String[] sqlColNames, List<String> sqlParams){
        //build grid filters
        boolean openParentheses = false;
        for(String param: params){
            if(!openParentheses){
                if(!"".equals(whereClause)){
                    whereClause += " AND ";
                }
                whereClause += "(";
                openParentheses = true;
            }else{
                whereClause += " OR ";
            }
            for(int j = 0 ; j < sqlColNames.length; j++){
                if(j > 0){
                    whereClause += " OR ";
                }
                whereClause += sqlColNames[j] + " = ?";
                sqlParams.add(param);
            }
        }
        if(openParentheses){
            whereClause += ")";
        }
        
        return whereClause;
    }
    
    static public String buildWhereClauseFromPost(String sql, JsonObject jsonCheckFilters, List<String> sqlParams){
        HashMap<String, List<String>> checkFilterDBMap = RESTUtil.createAdminFilterMap();
        for(String jsonField : checkFilterDBMap.keySet()){
            if(!"*".equals(RESTUtil.checkField(jsonField, jsonCheckFilters, false, false, "*") +"")){
                if(sqlParams.isEmpty()){
                    sql += " WHERE";
                }else{
                    sql += " AND";
                }
                boolean openParen = false;
                for(int i = 0 ; i < jsonCheckFilters.getJsonArray(jsonField).size(); i++){
                    if(i == 0){
                        sql += " (";
                        openParen = true;
                    }else{
                        sql += " OR";
                    }
                    
                    for(int j = 0 ; j < checkFilterDBMap.get(jsonField).size(); j++){
                        if(j > 0){
                            sql += " OR";
                        }
                        sql += " " + checkFilterDBMap.get(jsonField).get(j) + "=?";
                        sqlParams.add(jsonCheckFilters.getJsonArray(jsonField).getString(i));
                    }
                }
                if(openParen){
                    sql += " )";
                }
            }
        }

        return sql;
    }
}

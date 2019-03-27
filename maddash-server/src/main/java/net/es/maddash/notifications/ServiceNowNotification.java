package net.es.maddash.notifications;

import net.es.jsnow.ServiceNowClient;
import net.es.jsnow.oauth.OAuth2Details;
import net.es.jsnow.parameters.TableQueryParams;
import net.es.maddash.NetLogger;
import net.es.maddash.checks.TemplateVariableMap;
import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Creates records in ServiceNow (https://www.service-now.com) based on configuration provided
 */
public class ServiceNowNotification implements Notification{
    private Logger log = Logger.getLogger(ServiceNowNotification.class);
    private Logger netlogger = Logger.getLogger("netlogger");

    private String snowInstanceName;
    private String clientId;
    private String clientSecret;
    private String username;
    private String password;
    private String recordTable;
    private JsonObject recordFields;
    private String dashboardUrl;
    private JsonObject duplicateRules;

    final public static String PROP_SNOW_INSTANCE_NAME = "instance";
    final public static String PROP_OAUTH_FILE = "oauthFile";
    final public static String PROP_CLIENT_ID = "clientID";
    final public static String PROP_CLIENT_SECRET = "clientSecret";
    final public static String PROP_USERNAME = "username";
    final public static String PROP_PASSWORD = "password";
    final public static String PROP_RECORD_TABLE = "recordTable";
    final public static String PROP_RECORD_FIELDS = "recordFields";
    final public static String PROP_DASHBOARDURL = "dashboardUrl";
    final public static String PROP_DUPLICATERULES = "duplicateRules";
    final public static String PROP_DUPLICATERULES_RULES = "rules";
    final public static String PROP_RULES_IDENTITYFIELDS = "identityFields";
    final public static String PROP_RULES_EQUALSFIELDS = "equalsFields";
    final public static String PROP_RULES_LTFIELDS = "ltFields";
    final public static String PROP_RULES_GTFIELDS = "gtFields";
    final public static String PROP_RULES_UPDATEFIELDS = "updateFields";

    /**
     * Initializes ServiceNow client based on configuration
     * @param name name of the notification
     * @param params configuration options
     */
    public void init(String name, JsonObject params) {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.ServiceNowNotification.init"));
        //get required properties
        if(params.containsKey(PROP_SNOW_INSTANCE_NAME) && !params.isNull(PROP_SNOW_INSTANCE_NAME) ){
            this.snowInstanceName = params.getString(PROP_SNOW_INSTANCE_NAME);
        }else{
            throw new RuntimeException("The property " + PROP_SNOW_INSTANCE_NAME + " must be specified");
        }
        if(params.containsKey(PROP_RECORD_TABLE) && !params.isNull(PROP_RECORD_TABLE) ){
            this.recordTable = params.getString(PROP_RECORD_TABLE);
        }else{
            throw new RuntimeException("The property " + PROP_RECORD_TABLE + " must be specified");
        }
        if(params.containsKey(PROP_RECORD_FIELDS) && !params.isNull(PROP_RECORD_FIELDS) ){
            this.recordFields = params.getJsonObject(PROP_RECORD_FIELDS);
        }else{
            throw new RuntimeException("The property " + PROP_RECORD_FIELDS + " must be specified");
        }

        //get oauth props from file
        this.clientId = null;
        this.clientSecret = null;
        if(params.containsKey(PROP_OAUTH_FILE) && !params.isNull(PROP_OAUTH_FILE) ){
            this.loadOAuthFile(params.getString(PROP_OAUTH_FILE));
        }

        //get OAuth properties - overrides file props
        if(params.containsKey(PROP_USERNAME) && !params.isNull(PROP_USERNAME) ){
            this.username = params.getString(PROP_USERNAME);
        }else if(this.username == null){
            //if not set by property or OAuth file, then error
            throw new RuntimeException("The property " + PROP_USERNAME + " must be specified");
        }
        if(params.containsKey(PROP_PASSWORD) && !params.isNull(PROP_PASSWORD) ){
            this.password = params.getString(PROP_PASSWORD);
        }else if(this.password == null){
            //if not set by property or OAuth file, then error
            throw new RuntimeException("The property " + PROP_PASSWORD + " must be specified");
        }
        if(params.containsKey(PROP_CLIENT_ID) && !params.isNull(PROP_CLIENT_ID) ){
            this.clientId = params.getString(PROP_CLIENT_ID);
        }
        if(params.containsKey(PROP_CLIENT_SECRET) && !params.isNull(PROP_CLIENT_SECRET) ){
            this.clientSecret= params.getString(PROP_CLIENT_SECRET);
        }
        if(params.containsKey(PROP_DASHBOARDURL) && !params.isNull(PROP_DASHBOARDURL) ){
            this.dashboardUrl= params.getString(PROP_DASHBOARDURL);
        }else{
            this.dashboardUrl = null;
        }
        if(params.containsKey(PROP_DUPLICATERULES) && !params.isNull(PROP_DUPLICATERULES) ){
            this.duplicateRules= params.getJsonObject(PROP_DUPLICATERULES);
        }else{
            this.duplicateRules = null;
        }
        netlogger.info(netLog.end("maddash.ServiceNowNotification.init"));
    }

    private void loadOAuthFile(String file){
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.ServiceNowNotification.loadOAuthFile"));

        //read file
        Map oauthConfig = null;
        try {
            oauthConfig = (Map) Yaml.load(new File(file));
        } catch (FileNotFoundException e) {
            netlogger.error(netLog.error("maddash.ServiceNowNotification.loadOAuthFile", "Unable to find file " + file + ": " + e.getMessage()));
            return;
        }
        if(oauthConfig == null){
            //should not happen, but just to be safe
            return;
        }

        //get parameters
        if(oauthConfig.containsKey(PROP_CLIENT_ID) && oauthConfig.get(PROP_CLIENT_ID) != null){
            this.clientId = oauthConfig.get(PROP_CLIENT_ID) + "";
        }
        if(oauthConfig.containsKey(PROP_CLIENT_SECRET) && oauthConfig.get(PROP_CLIENT_SECRET) != null){
            this.clientSecret = oauthConfig.get(PROP_CLIENT_SECRET) + "";
        }
        if(oauthConfig.containsKey(PROP_USERNAME) && oauthConfig.get(PROP_USERNAME) != null){
            this.username = oauthConfig.get(PROP_USERNAME) + "";
        }
        if(oauthConfig.containsKey(PROP_PASSWORD) && oauthConfig.get(PROP_PASSWORD) != null){
            this.password = oauthConfig.get(PROP_PASSWORD) + "";
        }

        netlogger.info(netLog.end("maddash.ServiceNowNotification.loadOAuthFile"));
    }

    /**
     * Creates or updates ServiceNow records given a list of problems
     *
     * @param problems the problems from which to create notifications
     */
    public void send(List<NotifyProblem> problems) {
        NetLogger netLog = NetLogger.getTlogger();
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        netlogger.info(netLog.start("maddash.ServiceNowNotification.send"));
        //don't send anything if no problems
        if(problems.isEmpty()){
            netlogger.info(netLog.end("maddash.ServiceNowNotification.send", "No problems found so no records created"));
            return;
        }
        try {
            //Setup oauth2 details
            OAuth2Details oauth2Details = new OAuth2Details();
            oauth2Details.setClientId(this.clientId);
            oauth2Details.setClientSecret(this.clientSecret);
            oauth2Details.setUsername(this.username);
            oauth2Details.setPassword(this.password);
            //Build client
            ServiceNowClient snowclient = new ServiceNowClient(this.snowInstanceName, oauth2Details);
            //iterate through problems
            for (NotifyProblem p : problems) {
                this.handleProblem(snowclient, p);
            }
            netlogger.info(netLog.end("maddash.ServiceNowNotification.send", "Operation completed", null, netLogParams));
        }catch(Exception e){
            e.printStackTrace();
            netlogger.info(netLog.error("maddash.ServiceNowNotification.send", e.getMessage(), null, netLogParams));
        }
    }

    private void handleProblem(ServiceNowClient snowclient, NotifyProblem p){
        //init logging
        NetLogger netLog = NetLogger.getTlogger();
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        String netLogMsg = "";
        netlogger.info(netLog.start("maddash.ServiceNowNotification.handleProblem"));

        //don't send anything if no problems
        if(p == null || p.getProblem() == null){
            netlogger.info(netLog.end("maddash.ServiceNowNotification.handleProblem", "Problems null, so nothing to do"));
            return;
        }

        //load some logging fields
        netLogParams.put("problemGrid", p.getGridName());
        netLogParams.put("problemSite", p.getSiteName());
        netLogParams.put("problemName", p.getProblem().getName());
        netLogParams.put("problemCat", p.getProblem().getCategory());
        netLogParams.put("problemSeverity", p.getProblem().getSeverity()+"");

        //expand record template
        JsonObject expandedRecordField = this.expandRecordFields(p, this.recordFields);
        //check for duplicate record
        String duplicateRecordId = this.handleDuplicateRecord(snowclient, expandedRecordField, p);
        if (duplicateRecordId != null) {
            netLogParams.put("action", "update");
            //update happened in handleDuplicateRecord
        } else {
            netLogParams.put("action", "create");
            //JsonObject response = snowclient.createRecord(this.recordTable, expandedRecordField);
            //netLogParams.put("snowResponse", response+"");
            netLogMsg = "Created record from problem";
        }

        netlogger.info(netLog.end("maddash.ServiceNowNotification.handleProblem", netLogMsg, null, netLogParams));
    }

    private String handleDuplicateRecord(ServiceNowClient snowclient, JsonObject expandedRecordField, NotifyProblem p) {
        //Logger initialization
        NetLogger netLog = NetLogger.getTlogger();
        String netLogMsg = "";
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        netlogger.debug(netLog.start("maddash.ServiceNowNotification.handleDuplicateRecord"));

        //no duplicate rules then nothing to check
        if(this.duplicateRules == null){
            netlogger.debug(netLog.end("maddash.ServiceNowNotification.handleDuplicateRecord", "No duplicate rules so nothing to check"));
            return null;
        }

        //get id fields
        List<String> idQuery = new ArrayList<String>();
        if(this.duplicateRules.containsKey(PROP_RULES_IDENTITYFIELDS) &&
                !this.duplicateRules.isNull(PROP_RULES_IDENTITYFIELDS)){
            JsonArray idFields = this.duplicateRules.getJsonArray(PROP_RULES_IDENTITYFIELDS);
            for(int i = 0; i < idFields.size(); i++) {
                String idField = idFields.getString(i);
                if(expandedRecordField.containsKey(idField)) {
                    idQuery.add(idField + "=" + expandedRecordField.getString(idField + ""));
                }else{
                    String errMsg = "In the " + PROP_DUPLICATERULES +
                            " section of your configuration, an invalid " +
                            PROP_RULES_EQUALSFIELDS + "entry is given";
                    netlogger.debug(netLog.error("maddash.ServiceNowNotification.handleDuplicateRecord", errMsg));
                    throw new RuntimeException(errMsg);
                }
            }
        }

        //if no rules, then nothing to do
        if(!this.duplicateRules.containsKey(PROP_DUPLICATERULES_RULES) ||
                this.duplicateRules.isNull(PROP_DUPLICATERULES_RULES)) {
            netlogger.info(netLog.end("maddash.ServiceNowNotification.handleDuplicateRecord", netLogMsg, null, netLogParams));
            return null;
        }

        //loop through rules until find the first one that matches something
        JsonArray rules = this.duplicateRules.getJsonArray(PROP_DUPLICATERULES_RULES);
        for(int i = 0; i < rules.size(); i++) {
            JsonObject rule = rules.getJsonObject(i);
            List<String> snowQuery = new ArrayList<String>();
            snowQuery.addAll(idQuery);

            //get equals fields
            if (rule.containsKey(PROP_RULES_EQUALSFIELDS) && !rule.isNull(PROP_RULES_EQUALSFIELDS)) {
                JsonObject eqFields = rule.getJsonObject(PROP_RULES_EQUALSFIELDS);
                for (String eqField : eqFields.keySet()) {
                    snowQuery.add(eqField + "=" + eqFields.get(eqField));
                }
            }

            //get lt fields
            if (rule.containsKey(PROP_RULES_LTFIELDS) && !rule.isNull(PROP_RULES_LTFIELDS)) {
                JsonObject ltFields = rule.getJsonObject(PROP_RULES_LTFIELDS);
                for (String ltField : ltFields.keySet()) {
                    snowQuery.add(ltField + "<" + ltFields.get(ltField));
                }
            }

            //get gt fields
            if (rule.containsKey(PROP_RULES_GTFIELDS) && !rule.isNull(PROP_RULES_GTFIELDS)) {
                JsonObject gtFields = rule.getJsonObject(PROP_RULES_GTFIELDS);
                for (String gtField : gtFields.keySet()) {
                    snowQuery.add(gtField + ">" + gtFields.get(gtField));
                }
            }

            //build query string
            String snowQueryString = String.join("^", snowQuery);
            TableQueryParams queryParams = new TableQueryParams();
            queryParams.setQuery(snowQueryString);
            netLogParams.put("getParams", queryParams.toGetParams());

            //perform query
            JsonObject duplicateResults = snowclient.queryTable(this.recordTable, queryParams);
            if (duplicateResults.containsKey("result") && !duplicateResults.isNull("result")) {
                netLogParams.put("dupCount", duplicateResults.size() + "");
                if (!duplicateResults.getJsonArray("result").isEmpty()) {
                    //only grab the first result
                    JsonObject record = duplicateResults.getJsonArray("result").getJsonObject(0);
                    if (record.containsKey("sys_id") && !record.isNull("sys_id")) {
                        String duplicateRecordId = record.getString("sys_id");
                        netLogParams.put("duplicateRecordId", duplicateRecordId);
                        //check if we want to update the duplicate or do nothing
                        if (rule.containsKey(PROP_RULES_UPDATEFIELDS) && !rule.isNull(PROP_RULES_UPDATEFIELDS)) {
                            netLogParams.put("action", "update");
                            JsonObject updateObject = this.expandRecordFields(p, rule.getJsonObject(PROP_RULES_UPDATEFIELDS));
                            snowclient.updateRecord(this.recordTable, duplicateRecordId, updateObject);
                            netLogParams.put("duplicateRecordId", duplicateRecordId);
                            netLogMsg = "Updated record";
                        } else {
                            netLogParams.put("action", "none");
                            netLogMsg = "UpdateFields not configured so not updating duplicate record";
                        }
                        netlogger.info(netLog.end("maddash.ServiceNowNotification.handleDuplicateRecord", netLogMsg, null, netLogParams));
                        return duplicateRecordId;
                    }
                }
            }
        }

        netlogger.info(netLog.end("maddash.ServiceNowNotification.handleDuplicateRecord", netLogMsg, null, netLogParams));
        return null;
    }

    private JsonObject expandRecordFields(NotifyProblem p, JsonObject record) {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.debug(netLog.start("maddash.ServiceNowNotification.expandRecordFields"));
        HashMap<String,String> netLogParams = new HashMap<String,String>();

        //convert to string
        TemplateVariableMap templateVars = new TemplateVariableMap();
        String str = record + "";
        templateVars.put("%br", "\\\\n");
        if(p.getSiteName() != null) {
            templateVars.put("%problemEntity", p.getSiteName() + "");
        }else if(p.getGridName() != null){
            templateVars.put("%problemEntity", p.getGridName() + "");
        }else{
            templateVars.put("%problemEntity", "Unknown");
        }
        if(p.getGridName() != null && this.dashboardUrl != null){
            String gridUrl = this.dashboardUrl;
            if(!gridUrl.endsWith("/")){
                gridUrl += "/";
            }
            try {
                gridUrl += "index.cgi?grid=" + URLEncoder.encode(p.getGridName(), "UTF-8");
            }catch(Exception e){}
            templateVars.put("%gridUrl", gridUrl);
            String gridLink = "[code]<a href=\\\\\"" + gridUrl + "\\\\\" target=\\\\\"_blank\\\\\">View Grid</a>[/code]";
            templateVars.put("%gridLink", gridLink);
        }
        templateVars.put("%siteName", p.getSiteName() + "");
        templateVars.put("%gridName", p.getGridName()+ "");
        templateVars.put("%isGlobal", p.isGlobal() + "");
        templateVars.put("%severity", p.getProblem().getSeverity() + "");
        templateVars.put("%category", p.getProblem().getCategory()+ "");
        StringBuilder solutions = new StringBuilder();
        if(p.getProblem().getSolutions() != null && !p.getProblem().getSolutions().isEmpty()){
            for(String solution : p.getProblem().getSolutions()){
                solutions.append(" - ").append(solution).append("\\\\n");
            }
        }else{
            solutions.append(" - None available").append("\\\\n");
        }
        templateVars.put("%solutions", solutions.toString());
        templateVars.put("%name", p.getProblem().getName()+ "");

        //replace variables
        for(String var: templateVars.keySet()){
            String val = templateVars.get(var);
            if(val == null || val.toLowerCase().equals("null")) {
                val = "n/a";
            }
            str = str.replaceAll(var, val);
        }

        //create object
        JsonObject jsonResult = Json.createReader(new StringReader(str)).readObject();
        netLogParams.put("expandedJson", jsonResult + "");
        netlogger.debug(netLog.end("maddash.ServiceNowNotification.expandRecordFields",null,null, netLogParams));

        return jsonResult;
    }
}

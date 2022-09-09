package net.es.jsnow;

import net.es.jsnow.oauth.OAuth2Details;
import net.es.jsnow.oauth.OAuthUtils;
import net.es.jsnow.parameters.RecordReadParams;
import net.es.jsnow.parameters.RecordWriteParams;
import net.es.jsnow.parameters.TableParams;
import net.es.jsnow.parameters.TableQueryParams;

import javax.json.JsonObject;

/**
 * The primary client for interacting with the ServiceNow REST API. Currently only supports the
 * Table API and assumes OAuth authentication/authorization.
 */
public class ServiceNowClient {
    protected String snowName;
    protected OAuth2Details oauth2Details;

    /**
     * Constructor
     * @param snowName the name of the ServiceNow instance to contact
     */
    public ServiceNowClient(String snowName) {
        this.setSnowName(snowName);
    }

    /**
     * Constructor
     * @param snowName the name of the ServiceNow instance to contact
     * @param oauth2Details the OAuth parameters to use
     */
    public ServiceNowClient(String snowName, OAuth2Details oauth2Details) {
        this.setSnowName(snowName);
        this.setOAuth2Details(oauth2Details);
    }

    /**
     * Returns the name of the ServiceNow instance of this client
     * @return the ServiceNow instance name
     */
    public String getSnowName() {
        return snowName;
    }

    /**
     * Set the name of the ServiceNow instance of this client
     * @param snowName the ServiceNow instance name
     */
    public void setSnowName(String snowName) {
        this.snowName = snowName;
    }

    /**
     * Returns the OAuth parameters used by this client
     * @return the OAuth parameters used by this client
     */
    public OAuth2Details getOAuth2Details() {
        return oauth2Details;
    }

    /**
     * Sets the OAuth parameters used by this client
     * @param oauth2Details the OAuth parameters used by this client
     */
    public void setOAuth2Details(OAuth2Details oauth2Details) {
        if(oauth2Details.getAuthenticationServerUrl() == null){
            oauth2Details.setAuthenticationServerUrl(this.getOAuthTokenUrl());
        }
        this.oauth2Details = oauth2Details;
    }

    /**
     * @return the base URL used to access the instance.
     */
    public String getBaseSnowUrl(){
        return "https://" + this.snowName + ".service-now.com";
    }

    /**
     * @return the URL used to retrieve OAuth tokens
     */
    public String getOAuthTokenUrl(){
        return this.getBaseSnowUrl() + "/oauth_token.do";
    }

    /**
     * Creates a new record in the given table.
     * @param table the name of the table to update
     * @param recordPayload the JSON of the record to create
     * @param params the parameters to include with this request
     * @return the returned JSON object
     */
    public JsonObject createRecord(String table, JsonObject recordPayload, RecordWriteParams params){
        String url = this.buildFullUrl( "/api/now/table/" + table, params);
        return OAuthUtils.postProtectedJsonResource(url, recordPayload, this.oauth2Details);
    }

    /**
     * Creates a new record in the given table.
     * @param table the name of the table to update
     * @param recordPayload the JSON of the record to create
     * @return the created JSON object as returned by the server
     */
    public JsonObject createRecord(String table, JsonObject recordPayload){
        return this.createRecord(table, recordPayload, null);
    }

    /**
     * Update the given record using an HTTP PUT
     * @param table the name of the table to update
     * @param sys_id the sys_id of the record to update
     * @param recordPayload the JSON containing the fields to update
     * @param params the parameters to include with this request
     * @return the updated JSON object as returned by the server
     */
    public JsonObject updateRecord(String table, String sys_id, JsonObject recordPayload, RecordWriteParams params){
        String url = this.buildFullUrl("/api/now/table/" + table + "/" + sys_id, params);
        return OAuthUtils.putProtectedJsonResource(url, recordPayload, this.oauth2Details);
    }

    /**
     * Update the given record using an HTTP PUT
     * @param table the name of the table to update
     * @param sys_id the sys_id of the record to update
     * @param recordPayload the JSON containing the fields to update
     * @return the updated JSON object as returned by the server
     */
    public JsonObject updateRecord(String table, String sys_id, JsonObject recordPayload){
        return this.updateRecord(table, sys_id, recordPayload, null);
    }

    /**
     * Update the given record using an HTTP PATCH
     * @param table the name of the table to update
     * @param sys_id the sys_id of the record to update
     * @param recordPayload the JSON containing the fields to update
     * @param params the parameters to include with this request
     * @return the updated JSON object as returned by the server
     */
    public JsonObject patchRecord(String table, String sys_id, JsonObject recordPayload, RecordWriteParams params){
        String url = this.buildFullUrl("/api/now/table/" + table + "/" + sys_id, params);
        return OAuthUtils.patchProtectedJsonResource(url, recordPayload, this.oauth2Details);
    }

    /**
     * Update the given record using an HTTP PATCH
     * @param table the name of the table to update
     * @param sys_id the sys_id of the record to update
     * @param recordPayload the JSON containing the fields to update
     * @return the updated JSON object as returned by the server
     */
    public JsonObject patchRecord(String table, String sys_id, JsonObject recordPayload){
        return this.patchRecord(table, sys_id, recordPayload, null);
    }

    /**
     * Delete the given record
     * @param table the name of the table to update
     * @param sys_id the sys_id of the record to delete
     * @return the HTTP response as a JSON object
     */
    public JsonObject deleteRecord(String table, String sys_id){
        String url = this.buildFullUrl("/api/now/table/" + table + "/" + sys_id, null);
        return OAuthUtils.deleteProtectedJsonResource(url, this.oauth2Details);
    }

    /**
     * Query an individual record
     * @param table the name of the table to query
     * @param sys_id the ID of the record to update
     * @param params the query parameters to include with this request
     * @return the matching record
     */
    public JsonObject queryRecord(String table, String sys_id, RecordReadParams params){
        String url = this.buildFullUrl("/api/now/table/" + table + "/" + sys_id, params);
        return OAuthUtils.getProtectedJsonResource(url, this.oauth2Details);
    }

    /**
     * Query an individual record
     * @param table the name of the table to query
     * @param sys_id the ID of the record to update
     * @return the matching record
     */
    public JsonObject queryRecord(String table, String sys_id){
        return this.queryRecord(table, sys_id, null);
    }

    /**
     * Query the given table for a set of matching records
     * @param table the name of the table to query
     * @param params the query parameters to include with this request
     * @return the list of records that match the query
     */
    public JsonObject queryTable(String table, TableQueryParams params){
        String url = this.buildFullUrl("/api/now/table/" + table, params);
        return OAuthUtils.getProtectedJsonResource(url, this.oauth2Details);
    }

    /**
     * Query the given table for a set of matching records
     * @param table the name of the table to query
     * @return the list of records that match the query
     */
    public JsonObject queryTable(String table){
        return this.queryTable(table, null);
    }

    private String buildFullUrl(String path, TableParams params){
        String url = this.getBaseSnowUrl() + path;
        String getParams = "";
        if(params != null){
            getParams = params.toGetParams();
        }
        if(!getParams.isEmpty()){
            url += "?" + getParams;
        }

        return url;
    }

}

package net.es.jsnow.parameters;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.client.utils.URLEncodedUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The base class for defining parameters when making request to the Table API
 */
public abstract class TableParams {

    protected Boolean displayValue = null;
    protected ArrayList<String> fields = new ArrayList<String>();
    protected String view = null;

    /**
     * @return the value of sysparm_display_value
     */
    public Boolean isDisplayValue() {
        return displayValue;
    }

    /**
     * @param displayValue the value of sysparm_display_value
     */
    public void setDisplayValue(Boolean displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * @return the value of sysparm_fields
     */
    public ArrayList<String> getFields() {
        return fields;
    }

    /**
     * @param fields the value of sysparm_fields
     */
    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }

    /**
     * @return the value of sysparm_view
     */
    public String getView() {
        return view;
    }

    /**
     * @param view the value of sysparm_view
     */
    public void setView(String view) {
        this.view = view;
    }

    protected List<BasicNameValuePair> buildGetParams(){
        ArrayList<BasicNameValuePair> getParams = new ArrayList<BasicNameValuePair>();

        if(this.displayValue != null){
            if(this.displayValue){
                getParams.add(new BasicNameValuePair("sysparm_display_value", "true"));
            }else{
                getParams.add(new BasicNameValuePair("sysparm_display_value", "false"));
            }
        }

        if(!this.fields.isEmpty()){
            getParams.add(new BasicNameValuePair("sysparm_fields",String.join(",", this.fields)));

        }

        if(this.view != null){
            getParams.add(new BasicNameValuePair("sysparm_view", this.view));

        }

        return getParams;
    }

    /**
     * Converts properties in this class to HTTP GET-style parameter string
     * @return HTTP GET-style parameter string
     */
    public String toGetParams(){
        List<BasicNameValuePair> getParams = this.buildGetParams();

        return URLEncodedUtils.format(getParams, HTTP.UTF_8);
    }
}

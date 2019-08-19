package net.es.jsnow.parameters;

import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * Parameters to use when creating records
 */
public class RecordWriteParams extends TableParams{
    protected Boolean inputDisplayValue = null;

    /**
     * @return the value of sysparm_input_display_value
     */
    public Boolean getInputDisplayValue() {
        return inputDisplayValue;
    }

    /**
     * @param inputDisplayValue the value of sysparm_input_display_value
     */
    public void setInputDisplayValue(Boolean inputDisplayValue) {
        this.inputDisplayValue = inputDisplayValue;
    }

    @Override
    protected List<BasicNameValuePair> buildGetParams() {
        List<BasicNameValuePair> getParams = super.buildGetParams();

        if(this.inputDisplayValue != null){
            if(this.inputDisplayValue){
                getParams.add(new BasicNameValuePair("sysparm_input_display_value", "true"));
            }else{
                getParams.add(new BasicNameValuePair("sysparm_input_display_value", "false"));
            }
        }

        return getParams;
    }
}

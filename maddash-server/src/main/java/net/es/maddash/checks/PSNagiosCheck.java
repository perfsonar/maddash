package net.es.maddash.checks;

/**
 * 
 * DELETE THIS MOST LIKELY OR CHANGE IT. FOUND BETTER WAY TO GET KEY
 * 
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class PSNagiosCheck extends NagiosCheck implements Check {
    
    static HashMap<String, String> eventTypes = new  HashMap<String, String>();
    static{
        eventTypes.put("%event.delayBuckets", "http://ggf.org/ns/nmwg/characteristic/delay/summary/20110317");
        eventTypes.put("%event.delay", "http://ggf.org/ns/nmwg/characteristic/delay/summary/20070921");
        eventTypes.put("%event.bandwidth", "http://ggf.org/ns/nmwg/characteristics/bandwidth/achievable/2.0");
        eventTypes.put("%event.iperf", "http://ggf.org/ns/nmwg/tools/iperf/2.0");
        eventTypes.put("%event.utilization", "http://ggf.org/ns/nmwg/characteristic/utilization/2.0");
    }
    
    private final String PARAM_MD_KEY_LOOKUP = "metaDataKeyLookup";
    private final String PARAM_GRAPH_URL = "graphUrl";
    private final String PARAM_MAURL = "maUrl";
    
    public CheckResult check(String gridName, String rowName, String colName,
            Map params, int timeout) {
        
        //initialize replacement vars
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("%row", rowName);
        vars.put("%col", colName);
        vars.putAll(eventTypes);
        
        //get MA URL
        if(!params.containsKey(PARAM_MAURL) || params.get(PARAM_MAURL) == null){
            return new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                    PARAM_MAURL + " not defined. Please check config file", null);
        }
        Map maUrlMap = (Map)params.get(PARAM_MAURL);
        String maUrl = null;
        if(maUrlMap.containsKey(rowName) && maUrlMap.get(rowName) != null){
            maUrl = (String) maUrlMap.get(rowName);
        }else{
            maUrl = (String) maUrlMap.get("default");
        }
        if(maUrl == null){
            return new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                     "Default MA URL not defined. Please check config file", null);
        }
        maUrl = this.replaceVars(maUrl, vars);
        vars.put("%maUrl", maUrl);
        
        //get metadata key lookup URL
        if(!params.containsKey(PARAM_MD_KEY_LOOKUP) || params.get(PARAM_MD_KEY_LOOKUP) == null){
            return new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                    PARAM_MD_KEY_LOOKUP + " not defined. Please check config file", null);
        }
        String mdKeyLookupUrl = (String)params.get(PARAM_MD_KEY_LOOKUP);
        mdKeyLookupUrl = this.replaceVars(mdKeyLookupUrl, vars);
        
        //get graph url
        if(!params.containsKey(PARAM_GRAPH_URL) || params.get(PARAM_GRAPH_URL) == null){
            return new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                    PARAM_GRAPH_URL + " not defined. Please check config file", null);
        }
        String graphUrl = (String)params.get(PARAM_GRAPH_URL);
        
        //replace maUrl in command
        if(!params.containsKey(PARAM_COMMAND) || params.get(PARAM_COMMAND) == null){
            return new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                    "Command not defined. Please check config file", null);
        }
        String command = (String)params.get(PARAM_COMMAND);
        command = this.replaceVars(command, vars);
        params.put(PARAM_COMMAND, command);
        
        //run command
        CheckResult nagiosResult = super.check(gridName, rowName, colName, params, timeout);
        if(nagiosResult.getStats() == null){
            nagiosResult.setStats(new HashMap<String, String>());
        }
        nagiosResult.getStats().put("maUrl", maUrl);
        
        //get MA key
        try{
            URL url = new URL(mdKeyLookupUrl);
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            System.out.println("mdKeyLookupUrl=" + mdKeyLookupUrl);
            System.out.println("Status code: " + httpConn.getResponseCode());
            System.out.println("Response Message: " + httpConn.getResponseMessage());
            if(httpConn.getResponseCode()/200 != 1 ){
                return nagiosResult;
            }
            BufferedReader responseReader  = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String response = "";
            String line = null;
            while ((line = responseReader.readLine()) != null){
                response += (line + '\n');
            }
            System.out.println("Response: " + response);
            JSONObject responseJSON = JSONObject.fromObject(response);
            //make sure not to replace longer vars with shorter (i.e. dst and dstIP)
            vars.put("%maKeyF", ""+responseJSON.get("maKey"));
            vars.put("%maKeyR",""+responseJSON.get("maKeyR"));
            vars.put("%srcName", ""+responseJSON.get("src"));
            vars.put("%srcIP", ""+responseJSON.get("srcIP"));
            vars.put("%dstName", ""+responseJSON.get("dst"));
            vars.put("%dstIP", ""+responseJSON.get("dstIP"));
            vars.put("%eventType", ""+responseJSON.get("eventType"));
            graphUrl = this.replaceVars(graphUrl, vars);
            nagiosResult.getStats().put("graphUrl", graphUrl);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return nagiosResult;
    }

    private String replaceVars(String param, HashMap<String, String> vars) {
        for(String var : vars.keySet()){
            param = param.replaceAll(var, vars.get(var));
        }
        return param;
    }
}

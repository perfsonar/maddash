package net.es.maddash.checks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.maddash.NetLogger;
import net.sf.json.JSONObject;

/**
 * Subclass of nagios check that not only runs a Nagios command but also runs 
 * a metaDataKeyRequest to get keys to the data and adds a URL to a graph in 
 * the return parameters.Specific input parameters include:
 *      maUrl->default: a template for the MA url
 *      maUrl->%rowName: a url for a specific row
 *      metaDataKeyLookup: URL template to cgi script where key info can be retrieved (usually called metaKeyReq.cgi)
 *      graphUrl: Template for URL to graph data. In addition to %maUrl, %row and %col the special variables returned during key lookup can also be used:
 *              %maKeyF: The forward direction (src->dst) key for the measurement archive
 *              %maKeyR: The reverse direction (dst->src) key for the measurement archive
 *              %srcName: The source hostname
 *              %srcIP:The source IP address
 *              %dstName: The destination hostname
 *              %dstIP: The destination IP address
 *              %eventType: The eventType of teh data
 * All parameters support the special event type variables as well:
 *      %event.delayBuckets: http://ggf.org/ns/nmwg/characteristic/delay/summary/20110317
 *      %event.delay: http://ggf.org/ns/nmwg/characteristic/delay/summary/20070921");
 *      %event.bandwidth: http://ggf.org/ns/nmwg/characteristics/bandwidth/achievable/2.0");
 *      %event.iperf: http://ggf.org/ns/nmwg/tools/iperf/2.0");
 *      %event.utilization: http://ggf.org/ns/nmwg/characteristic/utilization/2.0");
 *
 *
 * @author Andy Lake<andy@es.net>
 *
 */
public class PSNagiosCheck extends NagiosCheck implements Check {
    private Logger log = Logger.getLogger(NagiosCheck.class);
    private Logger netlogger = Logger.getLogger("netlogger");
    
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
        HashMap<String, String> netLogParams = new HashMap<String, String>();
        NetLogger netLog = NetLogger.getTlogger();
        
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
        String response = "";
        try{
            netlogger.debug(netLog.start("maddash.PSNagiosCheck.getMdKey", null, mdKeyLookupUrl));
            URL url = new URL(mdKeyLookupUrl);
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            log.debug("mdKeyLookupUrl=" + mdKeyLookupUrl);
            log.debug("Status code: " + httpConn.getResponseCode());
            log.debug("Response Message: " + httpConn.getResponseMessage());
            netLogParams.put("responseCode", httpConn.getResponseCode()+"");
            if(httpConn.getResponseCode()/200 != 1 ){
                netlogger.debug(netLog.end("maddash.PSNagiosCheck.getMdKey", null, mdKeyLookupUrl, netLogParams));
                return nagiosResult;
            }
            BufferedReader responseReader  = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line = null;
            while ((line = responseReader.readLine()) != null){
                response += (line + '\n');
            }
            netlogger.debug(netLog.end("maddash.PSNagiosCheck.getMdKey", null, mdKeyLookupUrl, netLogParams));
            log.debug("Response: " + response);
           
        }catch(Exception e){
            netlogger.debug(netLog.error("maddash.PSNagiosCheck.getMdKey", null, mdKeyLookupUrl, netLogParams));
            log.error("Error getting metadata key: " + e.getMessage());
            e.printStackTrace();
        }
        
        
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
        
        return nagiosResult;
    }

    private String replaceVars(String param, HashMap<String, String> vars) {
        for(String var : vars.keySet()){
            param = param.replaceAll(var, vars.get(var));
        }
        return param;
    }
}

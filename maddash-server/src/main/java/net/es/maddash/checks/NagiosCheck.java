package net.es.maddash.checks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class NagiosCheck implements Check{
    
    final static public String PARAM_COMMAND = "command";
    
    public CheckResult check(String gridName, String rowName, String colName,
            Map params, int timeout) {
        if(!params.containsKey(PARAM_COMMAND) || params.get(PARAM_COMMAND) == null){
            return new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                    "Command not defined. Please check config file", null);
        }
        String command = (String)params.get(PARAM_COMMAND);
        command = command.replaceAll("%row", rowName);
        command = command.replaceAll("%col", colName);
        
        CheckResult result = null;
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            System.out.println("Executing command " + command);
            process = runtime.exec(command);
            WatchDog watchdog = new WatchDog(process);
            watchdog.start();
            watchdog.join(timeout*1000);
            if(watchdog.exit == null){
                result = new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                        "Command timed-out after " + timeout + " seconds",
                        null);
            }else{
                int resultCode = process.exitValue();
                String outputLine = null;
                Map returnParams = null;
                if(resultCode < CheckConstants.RESULT_SUCCESS || 
                        resultCode > CheckConstants.RESULT_UNKNOWN){
                    outputLine = "Unknown return status " + resultCode + " from command. Verify that it is a valid Nagios plug-in";
                    resultCode = CheckConstants.RESULT_UNKNOWN;
                }else{
                    BufferedReader stdin = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    outputLine = stdin.readLine();
                    returnParams = this.parseReturnParams(outputLine);
                    outputLine = this.formatOutputLine(outputLine);
                }
                result = new CheckResult(resultCode, outputLine, returnParams);
            }
        } catch (Exception e) {
            result = new CheckResult(CheckConstants.RESULT_UNKNOWN, 
                    "Exception executing command: " + e.getMessage(), null);
            e.printStackTrace();
        } finally{
            if(process != null){
                process.destroy();
            }
        }
        
        return result;
    }

    private String formatOutputLine(String outputLine) {
        outputLine = outputLine.replaceAll("^\\w+? \\w+? \\-", "");
        outputLine = outputLine.replaceAll("\\|.+", "");
        outputLine.trim();
            
        return outputLine;
    }

    private Map parseReturnParams(String outputLine) {
        HashMap<String, String> params = new HashMap<String, String>();
        String[] lineParts = outputLine.split("\\|");
        if(lineParts.length == 1){
            return null;
        }
        
        String statsString = lineParts[lineParts.length - 1];
        String[] kvPairs = statsString.split(";+");
        for(String kvPair : kvPairs){
            kvPair = kvPair.trim();
            String[] kv = kvPair.split("=");
            if(kv.length == 2){
                params.put(kv[0], kv[1]);
            }
        }
        
        return params;
    }

    private class WatchDog extends Thread{
        private Process process;
        private Integer exit = null;
        
        public WatchDog(Process process){
            this.process = process;
        }
        
        public void run(){
            try {
                this.exit = this.process.waitFor();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}

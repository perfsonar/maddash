package net.es.maddash.checks;

import java.util.Map;

public class CheckResult {
    private int resultCode;
    private String message;
    private Map stats;
    
    public CheckResult(int resultCode, String message, Map stats){
        this.resultCode = resultCode;
        this.message = message;
        this.stats = stats;
    }
    
    /**
     * @return the resultCode
     */
    public int getResultCode() {
        return this.resultCode;
    }

    /**
     * @param resultCode the resultCode to set
     */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the stats
     */
    public Map getStats() {
        return this.stats;
    }

    /**
     * @param stats the stats to set
     */
    public void setStats(Map stats) {
        this.stats = stats;
    }
}

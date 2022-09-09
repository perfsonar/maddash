package net.es.maddash.checks;

import java.util.Map;

/**
 * The type returned by a check when it is run. It contains three values:
 *     resultCode: Indicates status of check. It should be compared against values in CheckConstants.
 *     message: Message returned by the check describing the result
 *     stats: Check specific statistics that relate to the result
 *  
 * @author Andy Lake<andy@es.net>
 *
 */
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
     * @return Indicates status of check. It should be compared against values in CheckConstants.
     */
    public int getResultCode() {
        return this.resultCode;
    }

    /**
     * @param resultCode Indicates status of check. It should be compared against values in CheckConstants.
     */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * @return Message returned by the check describing the result
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @param message Message returned by the check describing the result
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return Check specific statistics that relate to the result
     */
    public Map getStats() {
        return this.stats;
    }

    /**
     * @param stats Check specific statistics that relate to the result
     */
    public void setStats(Map stats) {
        this.stats = stats;
    }
}

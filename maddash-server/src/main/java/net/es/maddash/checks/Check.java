package net.es.maddash.checks;

import java.util.Map;

/**
 * Interface that each check called by the scheduler must implement
 * @author Andy Lake<andy@es.net>
 *
 */
public interface Check {
    /**
     * Called by the scheduler when it is time to run the check
     * 
     * @param gridName the name of the grid containing this check 
     * @param rowName the name of the row containing this check
     * @param colName the name of the column containing this check
     * @param params a map of check-specific parameters defined in the check configuration
     * @param timeout the maximum amount of time to wait for this check
     * @return the result of the check
     */
    public CheckResult check(String gridName, String rowName, String colName, Map params, int timeout);
}

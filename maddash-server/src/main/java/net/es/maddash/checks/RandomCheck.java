package net.es.maddash.checks;

import java.util.Map;
import java.util.Random;

/**
 * Implementation of Check that returnes a random result code. Primarily useful for testing.
 * @author Andy Lake <andy@es.net>
 *
 */
public class RandomCheck implements Check{

    public CheckResult check(String gridName, String rowName, String colName,
            Map params, int timeout) {
        Random rand = new Random();
        int resultCode = rand.nextInt(CheckConstants.RESULT_UNKNOWN+1);
        return new CheckResult(resultCode, "Random result", null);
    }

}

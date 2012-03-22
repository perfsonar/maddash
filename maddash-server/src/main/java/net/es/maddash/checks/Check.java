package net.es.maddash.checks;

import java.util.Map;

public interface Check {
    public CheckResult check(String gridName, String rowName, String colName, Map params, int timeout);
}

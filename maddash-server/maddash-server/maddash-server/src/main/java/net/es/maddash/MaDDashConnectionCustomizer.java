package net.es.maddash;

import java.sql.Connection;

import com.mchange.v2.c3p0.AbstractConnectionCustomizer;

/**
 * Custom class to set JDBC connection parameters in c3p0 pool. Current sets 
 * transactionIsolation to TRANSACTION_READ_UNCOMMITTED. This does not require
 * SELECT statements to get any locks. 
 * 
 * @author Andy Lake <andy@es.net>
 *
 */
public class MaDDashConnectionCustomizer extends AbstractConnectionCustomizer{

    public void onAcquire(Connection conn, String parentDataSourceIdentityToken) throws Exception {
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }
    
}


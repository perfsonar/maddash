package net.es.maddash.notifications;


import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import org.apache.log4j.Logger;

import javax.json.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class for implementing notifications
 */
public abstract class BaseNotification implements Notification{
    private Logger netlogger = Logger.getLogger("netlogger");
    private Logger log = Logger.getLogger(BaseNotification.class);

    public void init(String name, JsonObject params){
        throw new RuntimeException("Notification class must implement init");
    }

    public void send(int notificationId, List<NotifyProblem> problems, List<String> resolvedData){
        throw new RuntimeException("Notification class must implement send");
    }

    protected void updateAppData(int notificationId, NotifyProblem p, String appData){
        NetLogger netLog = NetLogger.getTlogger();
        HashMap<String,String> netLogParams = new HashMap<String,String>();
        netLogParams.put("appData", appData);
        netLogParams.put("notificationId", notificationId + "");
        netLogParams.put("problemChecksum", p.checksum());
        netlogger.info(netLog.start("maddash.BaseNotification.updateAppData", null, null, netLogParams));
        //query database
        Connection conn = null;
        try {
            //init db and find notification row
            MaDDashGlobals globals = MaDDashGlobals.getInstance();
            conn = globals.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE notificationProblems SET appData =? WHERE notificationId=? AND checksum=?");
            stmt.setString(1, appData);
            stmt.setInt(2, notificationId);
            stmt.setString(3, p.checksum());
            stmt.executeUpdate();
            netlogger.info(netLog.end("maddash.BaseNotification.updateAppData", null, null, netLogParams));
        }catch(Exception e){
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            netlogger.info(netLog.error("maddash.BaseNotification.updateAppData", e.getMessage()));
            log.error("Error updating notification appdata " + e.getMessage());
            e.printStackTrace();
        }finally {
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
        }
    }

}
